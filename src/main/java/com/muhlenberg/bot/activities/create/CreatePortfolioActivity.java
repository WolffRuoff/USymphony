package com.muhlenberg.bot.activities.create;

import java.io.IOException;
import java.util.ArrayList;
import java.util.HashMap;

import com.fasterxml.jackson.databind.JsonNode;
import com.github.jknack.handlebars.Handlebars;
import com.github.jknack.handlebars.Template;
import com.github.jknack.handlebars.io.ClassPathTemplateLoader;
import com.github.jknack.handlebars.io.TemplateLoader;
import com.muhlenberg.bot.Database;
import com.muhlenberg.bot.HelperSource;
import com.muhlenberg.bot.ObjectToContext;
import com.muhlenberg.models.AddClients;
import com.muhlenberg.models.Portfolio;
import com.muhlenberg.models.Stock;
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
    return context -> "create-portfolio".equals(context.getFormId()) && "submit".equals(context.getFormValue("action"));
  }

  @Override
  public void onActivity(FormReplyContext context) {
    // Load handlebars stuff
    TemplateLoader loader = new ClassPathTemplateLoader();
    loader.setPrefix("/templates/create");
    loader.setSuffix(".hbs");
    Handlebars handlebars = new Handlebars(loader);
    handlebars.registerHelpers(new HelperSource());
    Template template;

    final String name = context.getFormValue("name");
    String ticker = context.getFormValue("ticker");
    if (ticker.isEmpty()) {
      ticker = "^GSPC";
    }

    // Convert JsonNode values to an ArrayList of userIDs
    JsonNode node = context.getFormValues();
    ArrayList<Long> clients = new ArrayList<Long>();
    for (JsonNode client : node.path("client-selector")) {
      clients.add(client.asLong());
    }
    if (clients.size() > 0) {

      // Sends user new form to input client amounts
      try {
        template = handlebars.compile("addClients");
        this.messageService.send(context.getSourceEvent().getStream(), template.apply(ObjectToContext.Convert(new AddClients(name, ticker, clients))));
      } catch (IOException e) {
        e.printStackTrace();
      }
    } else {
      // no clients!
      // Create portfolio and add it to the database
      final Portfolio p = new Portfolio(name, 0, 1.0D, new HashMap<Long, Double>(), new HashMap<Stock, Double>(),
          ticker);
      String finalName = Database.addPortfolio(context.getInitiator().getUser(), p);

      // Send confirmation message
      final String message = "<messageML>Created '" + finalName + "' Portfolio</messageML>";
      this.messageService.send(context.getSourceEvent().getStream(), Message.builder().content(message).build());
    }
  }

  @Override
  protected ActivityInfo info() {
    return new ActivityInfo().type(ActivityType.FORM).name("Create Portfolio Listener")
        .description("\"Form handler for the Create Portfolio form\"");
  }
}
