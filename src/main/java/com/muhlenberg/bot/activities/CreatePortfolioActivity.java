package com.muhlenberg.bot.activities;

import java.io.IOException;
import java.util.ArrayList;

import com.fasterxml.jackson.databind.JsonNode;
import com.github.jknack.handlebars.Context;
import com.github.jknack.handlebars.Handlebars;
import com.github.jknack.handlebars.Template;
import com.github.jknack.handlebars.io.ClassPathTemplateLoader;
import com.github.jknack.handlebars.io.TemplateLoader;
import com.muhlenberg.bot.ObjectToContext;
import com.muhlenberg.models.AddClients;
import com.symphony.bdk.core.activity.ActivityMatcher;
import com.symphony.bdk.core.activity.form.FormReplyActivity;
import com.symphony.bdk.core.activity.form.FormReplyContext;
import com.symphony.bdk.core.activity.model.ActivityInfo;
import com.symphony.bdk.core.activity.model.ActivityType;
import com.symphony.bdk.core.service.message.MessageService;

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
    // Load handlebars stuff
    TemplateLoader loader = new ClassPathTemplateLoader();
    loader.setPrefix("/templates");
    loader.setSuffix(".hbs");
    Handlebars handlebars = new Handlebars(loader);
    Template template;
    
    final String name = context.getFormValue("name");
    final String ticker = context.getFormValue("ticker");

    //Convert JsonNode values to an ArrayList of userIDs
    JsonNode node = context.getFormValues();
    ArrayList<Long> clients = new ArrayList<Long>(); 
    for (JsonNode client : node.path("client-selector")) {
      clients.add(client.asLong());
    }

    //Sends user new form to input client amounts
    Context c;
    try {
      template = handlebars.compile("addClients");
      c = ObjectToContext.Convert(new AddClients(name, ticker, clients));
      this.messageService.send(context.getSourceEvent().getStream(), template.apply(c));
    } catch (IOException e) {
      e.printStackTrace();
    }
  }
  

  @Override
  protected ActivityInfo info() {
    return new ActivityInfo().type(ActivityType.FORM)
        .name("Create Portfolio Listener")
        .description("\"Form handler for the Create Portfolio form\"");
  }
}
