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
public class SelectPortfolioView extends FormReplyActivity<FormReplyContext> {

  private final MessageService messageService;

  public SelectPortfolioView(MessageService messageService) {
    this.messageService = messageService;
  }

  @Override
  public ActivityMatcher<FormReplyContext> matcher() {
    return context -> "selectPortfolio".equals(context.getFormId())
        && "select-portfolio".equals(context.getFormValue("action"));
  }

  @Override
  public void onActivity(FormReplyContext context) {
    V4User user = context.getInitiator().getUser();

    // handlebars setup
    TemplateLoader loader = new ClassPathTemplateLoader();
    loader.setPrefix("/templates");
    loader.setSuffix(".hbs");
    Handlebars handlebars = new Handlebars(loader);
    Template template;

    // Retrieve portfolio selected
    String choice = context.getFormValue("portfolios");

    // If new-port create new portfolio
    if (choice.equals("new-port")) {
      try {
        template = handlebars.compile("create/createPortfolio");
        this.messageService.send(context.getSourceEvent().getStream(), template.apply(user));
      } catch (IOException e) {
        e.printStackTrace();
      }
    }
    // Otherwise determine which workflow and continue
    else {
      String nextStep = context.getFormValue("workflow");

      // If in the /portfolio workflow
      if (nextStep.equals("view")) {
        try {
          template = handlebars.compile("portfolio/clientBreakdownOrSummary");
          this.messageService.send(context.getSourceEvent().getStream(), template.apply(choice));
        } catch (IOException e) {
          e.printStackTrace();
        }
      }
      // If in the /buy workflow
      else if (nextStep.equals("buy")) {
        try {
          template = handlebars.compile("buy/buyAsset");
          this.messageService.send(context.getSourceEvent().getStream(), template.apply(choice));
        } catch (IOException e) {
          e.printStackTrace();
        }
      }
      // If in the /sell workflow
      else if (nextStep.equals("sell")) {
        try {
          template = handlebars.compile("buyAsset");
          this.messageService.send(context.getSourceEvent().getStream(), template.apply(choice));
        } catch (IOException e) {
          e.printStackTrace();
        }
      }
      // If in the /view workflow
      else if (nextStep.equals("client")) {
        try {
          handlebars.registerHelpers(new HelperSource());
          template = handlebars.compile("portfolio/summary");
          Portfolio p = Database.getPortfolio(user, choice, true);
          Context c = ObjectToContext.Convert(new Summary(p));
          this.messageService.send(context.getSourceEvent().getStream(), template.apply(c));
        } catch (IOException e) {
          e.printStackTrace();
        }
      }
    }

  }

  @Override
  protected ActivityInfo info() {
    return new ActivityInfo().type(ActivityType.FORM).name("Name of the Portfolio")
        .description("\"Form handler for the Create Portfolio form\"");
  }
}
