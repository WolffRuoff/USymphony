package com.muhlenberg.bot.activities;

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
public class BuyConfirm extends FormReplyActivity<FormReplyContext> {

  private final MessageService messageService;

  public BuyConfirm(MessageService messageService) {
    this.messageService = messageService;
  }

  @Override
  public ActivityMatcher<FormReplyContext> matcher() {
    return context -> "confirm-Buy".equals(context.getFormId()) && "submit".equals(context.getFormValue("action"));
  }

  @Override
  public void onActivity(FormReplyContext context) {
    V4User user = context.getInitiator().getUser();
    // Load handlebars stuff
    TemplateLoader loader = new ClassPathTemplateLoader();
    loader.setPrefix("/templates");
    loader.setSuffix(".hbs");
    Handlebars handlebars = new Handlebars(loader);
    Template template;

    // Retrieve their choices and the portfolio they chose
    String ticker = context.getFormValue("ticker");
    Double shares = Double.parseDouble(context.getFormValue("shares"));
    Double price = Double.parseDouble(context.getFormValue("price"));
    Double orderAmount = Double.parseDouble(context.getFormValue("orderAmount"));
    String portName = context.getFormValue("portfolio");
    Portfolio p = Database.getPortfolio(user, portName);

    // Place order
    Database.placeOrder(user, p,ticker,shares,price,orderAmount);
    try {
      price = YahooFinance.get(ticker).getQuote().getPrice().doubleValue();


      //Convert to object and send order confirmation
      handlebars.registerHelpers(new HelperSource());
      OrderDetails orderDets = new OrderDetails(p, ticker, shares, price, orderAmount);
      try {
        template = handlebars.compile("orderConfirmation");
        Context c = ObjectToContext.Convert(orderDets);
        this.messageService.send(context.getSourceEvent().getStream(), template.apply(c));
      } catch (IOException e) {
        e.printStackTrace();
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