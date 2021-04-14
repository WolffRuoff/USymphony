package com.muhlenberg.bot.activities;

import java.io.IOException;

import com.fasterxml.jackson.core.JsonProcessingException;
import com.fasterxml.jackson.databind.JsonNode;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.github.jknack.handlebars.Context;
import com.github.jknack.handlebars.Handlebars;
import com.github.jknack.handlebars.JsonNodeValueResolver;
import com.github.jknack.handlebars.Template;
import com.github.jknack.handlebars.context.FieldValueResolver;
import com.github.jknack.handlebars.context.JavaBeanValueResolver;
import com.github.jknack.handlebars.context.MapValueResolver;
import com.github.jknack.handlebars.context.MethodValueResolver;
import com.github.jknack.handlebars.io.ClassPathTemplateLoader;
import com.github.jknack.handlebars.io.TemplateLoader;
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
    TemplateLoader loader = new ClassPathTemplateLoader();
    loader.setPrefix("/templates");
    loader.setSuffix(".hbs");
    Handlebars handlebars = new Handlebars(loader);
    Template template;
    System.out.println(context.getFormValues().toPrettyString());
    String choice = context.getFormValue("options");
    System.out.println(choice);
    if (choice.equals("summary")) {
      System.out.println("worked");
      /*try {
        
        Context c = objectToContext(new Summary(p));

        this.messageService.send(context.getSourceEvent().getStream(), template.apply(c));
        template = handlebars.compile("createPortfolio");
        this.messageService.send(context.getSourceEvent().getStream(), template.apply(user));
      } catch (IOException e) {
        e.printStackTrace();
      }*/
    } else {
      try {
        template = handlebars.compile("clientBreakdown");
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

  public Context objectToContext(Object object) throws JsonProcessingException {
    ObjectMapper obj = new ObjectMapper();
    String jsonString = obj.writerWithDefaultPrettyPrinter().writeValueAsString(object);
    JsonNode jsonNode = new ObjectMapper().readValue(jsonString, JsonNode.class);
    Context c = Context.newBuilder(jsonNode).resolver(JsonNodeValueResolver.INSTANCE, JavaBeanValueResolver.INSTANCE,
        FieldValueResolver.INSTANCE, MapValueResolver.INSTANCE, MethodValueResolver.INSTANCE).build();

    return c;
  }
}