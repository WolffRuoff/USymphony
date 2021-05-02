package com.muhlenberg.bot.activities.buy;

import com.muhlenberg.bot.Database;
import com.muhlenberg.bot.HelperSource;
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

    // Retrieve their choices and the portfolio they chose
    String ticker = context.getFormValue("ticker");
    Double shares = Double.parseDouble(context.getFormValue("shares"));
    Double price = Double.parseDouble(context.getFormValue("price"));
    Double orderAmount = Double.parseDouble(context.getFormValue("orderAmount"));
    String portName = context.getFormValue("portfolio");
    Portfolio p = Database.getPortfolio(user, portName, false);

    // Place order
    Database.placeOrder(user, p, ticker, shares, price, orderAmount);

    // Send confirmation message
    HelperSource help = new HelperSource();
    final String message = "<messageML>Purchased " + help.round(3, shares) + " shares of '" + ticker + "' for $" + help.round(2, orderAmount) + ".</messageML>";
    this.messageService.send(context.getSourceEvent().getStream(), Message.builder().content(message).build());
  }

  @Override
  protected ActivityInfo info() {
    return new ActivityInfo().type(ActivityType.FORM).name("Buy Confirm Activity")
        .description("\"Form handler that submits the asset order\"");
  }
}