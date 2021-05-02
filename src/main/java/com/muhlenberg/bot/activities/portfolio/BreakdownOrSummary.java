package com.muhlenberg.bot.activities.portfolio;

import java.io.IOException;

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
import com.symphony.bdk.core.service.message.model.Message;
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
    loader.setPrefix("/templates/portfolio");
    loader.setSuffix(".hbs");
    Handlebars handlebars = new Handlebars(loader);
    handlebars.registerHelpers(new HelperSource());
    Template template;

    // Retrieve their choice and the portfolio they chose
    String choice = context.getFormValue("options");
    String portName = context.getFormValue("portfolio");
    Portfolio p = Database.getPortfolio(user, portName, false);
    // Make sure the user who submitted the form owns the portfolio
    if (p == null) {
      final String message = "<messageML><div style=\"color:red;\">'" + portName
          + "' doesn't exist or you are not authorized.</div></messageML>";
      this.messageService.send(context.getSourceEvent().getStream(), Message.builder().content(message).build());
      return;
    }

    if (choice.equals("summary")) {
      try {
        template = handlebars.compile("summary");
        this.messageService.send(context.getSourceEvent().getStream(),
            template.apply(ObjectToContext.Convert(new Summary(p))));
      } catch (IOException e) {
        e.printStackTrace();
      }
    } else {
      try {
        template = handlebars.compile("clientBreakdown");
        p.rebalancePortfolio();
        ClientList clients = new ClientList(p.getClientBreakdown(), p.getSize());
        this.messageService.send(context.getSourceEvent().getStream(),
            template.apply(ObjectToContext.Convert(clients)));
      } catch (IOException e) {
        e.printStackTrace();
      }
    }
  }

  @Override
  protected ActivityInfo info() {
    return new ActivityInfo().type(ActivityType.FORM).name("Breakdown or Summary Activity")
        .description("\"Form handler for the Breakdown or Summary form\"");
  }

}