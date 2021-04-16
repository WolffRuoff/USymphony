package com.muhlenberg.bot.activities;

import java.util.HashMap;

import com.fasterxml.jackson.databind.JsonNode;
import com.muhlenberg.bot.Database;
import com.muhlenberg.models.Portfolio;
import com.symphony.bdk.core.activity.ActivityMatcher;
import com.symphony.bdk.core.activity.form.FormReplyActivity;
import com.symphony.bdk.core.activity.form.FormReplyContext;
import com.symphony.bdk.core.activity.model.ActivityInfo;
import com.symphony.bdk.core.activity.model.ActivityType;
import com.symphony.bdk.core.service.message.MessageService;
import com.symphony.bdk.core.service.message.model.Message;

import org.springframework.stereotype.Component;

@Component
public class CreatePortfolioActivity extends FormReplyActivity<FormReplyContext> {

  private final MessageService messageService;

  public CreatePortfolioActivity(MessageService messageService) {
    this.messageService = messageService;
  }

  @Override
  public ActivityMatcher<FormReplyContext> matcher() {
    return context -> "create-portfolio".equals(context.getFormId())
        && "submit".equals(context.getFormValue("action"));
  }

  @Override
  public void onActivity(FormReplyContext context) {
    final String name = context.getFormValue("name");
    final String ticker = context.getFormValue("ticker");

    JsonNode node = context.getFormValues();
    HashMap<Long, Double> clients = new HashMap<Long, Double>(); 
    for (JsonNode client : node.path("client-selector")) {
      clients.put(client.asLong(), 0.0);
    }

    final Portfolio p = new Portfolio(name, clients, ticker);

    Database.addPortfolio(context.getInitiator().getUser(), p);

    final String message = "<messageML>Created '" + name + "' Portfolio</messageML>";
    this.messageService.send(context.getSourceEvent().getStream(), Message.builder().content(message).build());
  }

  @Override
  protected ActivityInfo info() {
    return new ActivityInfo().type(ActivityType.FORM)
        .name("Name of the Portfolio")
        .description("\"Form handler for the Create Portfolio form\"");
  }
}
