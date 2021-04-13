package com.muhlenberg.bot.activities;

import java.io.IOException;

import com.github.jknack.handlebars.Handlebars;
import com.github.jknack.handlebars.Template;
import com.github.jknack.handlebars.io.ClassPathTemplateLoader;
import com.github.jknack.handlebars.io.TemplateLoader;
import com.symphony.bdk.core.activity.ActivityMatcher;
import com.symphony.bdk.core.activity.form.FormReplyActivity;
import com.symphony.bdk.core.activity.form.FormReplyContext;
import com.symphony.bdk.core.activity.model.ActivityInfo;
import com.symphony.bdk.core.activity.model.ActivityType;
import com.symphony.bdk.core.service.message.MessageService;
import com.symphony.bdk.gen.api.model.V4User;

import org.springframework.stereotype.Component;

@Component
public class SelectPortfolio extends FormReplyActivity<FormReplyContext> {

  private final MessageService messageService;

  public SelectPortfolio(MessageService messageService) {
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
    TemplateLoader loader = new ClassPathTemplateLoader();
    loader.setPrefix("/templates");
    loader.setSuffix(".hbs");
    Handlebars handlebars = new Handlebars(loader);
    Template template;
    String choice = context.getFormValue("portfolios");
    System.out.println(choice);
    if (choice.equals("new-port")) {
      try {
        template = handlebars.compile("createPortfolio");
        this.messageService.send(context.getSourceEvent().getStream(), template.apply(user));
      } catch (IOException e) {
        e.printStackTrace();
      }
    }
    else {
      try {
        template = handlebars.compile("viewPortfolio");
        this.messageService.send(context.getSourceEvent().getStream(), template.apply(choice));
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
