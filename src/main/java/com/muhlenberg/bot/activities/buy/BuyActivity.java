package com.muhlenberg.bot.activities.buy;

import java.io.IOException;

import com.github.jknack.handlebars.Context;
import com.github.jknack.handlebars.Handlebars;
import com.github.jknack.handlebars.Template;
import com.github.jknack.handlebars.io.ClassPathTemplateLoader;
import com.github.jknack.handlebars.io.TemplateLoader;
import com.muhlenberg.bot.Database;
import com.muhlenberg.bot.HelperSource;
import com.muhlenberg.bot.ObjectToContext;
import com.muhlenberg.models.OrderDetails;
import com.muhlenberg.models.Portfolio;
import com.symphony.bdk.core.activity.ActivityMatcher;
import com.symphony.bdk.core.activity.form.FormReplyActivity;
import com.symphony.bdk.core.activity.form.FormReplyContext;
import com.symphony.bdk.core.activity.model.ActivityInfo;
import com.symphony.bdk.core.activity.model.ActivityType;
import com.symphony.bdk.core.service.message.MessageService;
import com.symphony.bdk.core.service.message.model.Message;
import com.symphony.bdk.gen.api.model.V4User;

import org.springframework.stereotype.Component;

import yahoofinance.YahooFinance;

@Component
public class BuyActivity extends FormReplyActivity<FormReplyContext> {

  private final MessageService messageService;

  public BuyActivity(MessageService messageService) {
    this.messageService = messageService;
  }

  @Override
  public ActivityMatcher<FormReplyContext> matcher() {
    return context -> "buy-asset".equals(context.getFormId()) && "submit".equals(context.getFormValue("action"));
  }

  @Override
  public void onActivity(FormReplyContext context) {
    V4User user = context.getInitiator().getUser();
    // Load handlebars stuff
    TemplateLoader loader = new ClassPathTemplateLoader();
    loader.setPrefix("/templates/buy");
    loader.setSuffix(".hbs");
    Handlebars handlebars = new Handlebars(loader);
    handlebars.registerHelpers(new HelperSource());
    Template template;

    // Retrieve their choices and the portfolio they chose
    String sharesOrPrice = context.getFormValue("buyOptions");
    String ticker = context.getFormValue("ticker");
    String portName = context.getFormValue("portfolio");
    Portfolio p = Database.getPortfolio(user, portName);

    // Retrieve the ticker price
    Double price = 1.00;
    Double shares = 1.00;
    Double orderAmount = 1.00;
    try {
      price = YahooFinance.get(ticker).getQuote().getPrice().doubleValue();

      // Convert shares to dollar value
      if (sharesOrPrice.equals("shares")) {
        shares = Double.parseDouble(context.getFormValue("Amount"));
        orderAmount = shares * price;
      }
      // Convert dollar value to shares
      else {
        orderAmount = Double.parseDouble(context.getFormValue("Amount"));
        shares = orderAmount / price;
      }

      // Make sure portfolio has enough liquid for the purchase
      Double liquidAmount =  p.getSize() * p.getPortionLiquid();
      if (liquidAmount >= orderAmount) {

        // Convert to object and send order confirmation
        OrderDetails orderDets = new OrderDetails(p, ticker, shares, price, orderAmount);
        try {
          template = handlebars.compile("orderConfirmation");
          Context c = ObjectToContext.Convert(orderDets);
          this.messageService.send(context.getSourceEvent().getStream(), template.apply(c));
        } catch (IOException e) {
          e.printStackTrace();
        }
      }
      // Portfolio doesn't have enough liquid for purchase
      else {
        final String message = "<messageML><div style=\"color:red;\">'" + p.getName() + "' doesn't have enough liquid assets. It only has $"
            + (Math.round(liquidAmount * 100.0) / 100.0) + ". Please try again.</div></messageML>";
        this.messageService.send(context.getSourceEvent().getStream(), Message.builder().content(message).build());
        template = handlebars.compile("buyAsset");
        this.messageService.send(context.getSourceEvent().getStream(), template.apply(p.getName()));
      }
    }

    // Send invalid ticker message and then buyActivity form again
    catch (IOException | NullPointerException e1) {
      try {
        final String message = "<messageML>'" + ticker + "' name was invalid. Please try again.</messageML>";
        this.messageService.send(context.getSourceEvent().getStream(), Message.builder().content(message).build());
        template = handlebars.compile("buyAsset");
        this.messageService.send(context.getSourceEvent().getStream(), template.apply(p.getName()));
      } catch (IOException e) {
        e.printStackTrace();
      }
      e1.printStackTrace();
    }
  }

  @Override
  protected ActivityInfo info() {
    return new ActivityInfo().type(ActivityType.FORM).name("Name of the Portfolio")
        .description("\"Form handler for the buyActivity form\"");
  }

}