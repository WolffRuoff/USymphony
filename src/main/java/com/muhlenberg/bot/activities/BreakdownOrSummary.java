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
import com.muhlenberg.models.ClientList;
import com.muhlenberg.models.Portfolio;
import com.muhlenberg.models.Summary;
import com.symphony.bdk.core.activity.ActivityMatcher;
import com.symphony.bdk.core.activity.form.FormReplyActivity;
import com.symphony.bdk.core.activity.form.FormReplyContext;
import com.symphony.bdk.core.activity.model.ActivityInfo;
import com.symphony.bdk.core.activity.model.ActivityType;
import com.symphony.bdk.core.service.message.MessageService;
import com.symphony.bdk.gen.api.model.V4User;

import org.springframework.stereotype.Component;

@Component
public class BreakdownOrSummary extends FormReplyActivity<FormReplyContext> {

  private final MessageService messageService;

  public BreakdownOrSummary(MessageService messageService) {
    this.messageService = messageService;
  }

  @Override
  public ActivityMatcher<FormReplyContext> matcher() {
    return context -> "clientBreakdownOrSummary".equals(context.getFormId())
        && "clientBreakdownOrSummary".equals(context.getFormValue("action"));
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

    // Retrieve their choice and the portfolio they chose
    String choice = context.getFormValue("options");
    String portName = context.getFormValue("portfolio");
    Portfolio p = Database.getPortfolio(user, portName);

    if (choice.equals("summary")) {
      handlebars.registerHelpers(new HelperSource());
      //handlebars.registerHelper("json", Jackson2Helper.INSTANCE);
      try {
        template = handlebars.compile("summary");
        Context c = ObjectToContext.Convert(new Summary(p));
        this.messageService.send(context.getSourceEvent().getStream(), template.apply(c));
      } catch (IOException e) {
        e.printStackTrace();
      }
    } else {
      try {
        template = handlebars.compile("clientBreakdown");
        ClientList clients = new ClientList(p.getClientBreakdown(), p.getSize());
        this.messageService.send(context.getSourceEvent().getStream(), template.apply(ObjectToContext.Convert(clients)));
      } catch (IOException e) {
        e.printStackTrace();
      }
    }

  }

  @Override
  protected ActivityInfo info() {
    return new ActivityInfo().type(ActivityType.FORM).name("Name of the Portfolio")
        .description("\"Form handler for the Create Portfolio form\"");
  }

}