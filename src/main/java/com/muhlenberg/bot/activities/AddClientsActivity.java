package com.muhlenberg.bot.activities;

import java.util.HashMap;
import java.util.Iterator;

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
public class AddClientsActivity extends FormReplyActivity<FormReplyContext> {

    private final MessageService messageService;

    public AddClientsActivity(MessageService messageService) {
        this.messageService = messageService;
    }

    @Override
    public ActivityMatcher<FormReplyContext> matcher() {
        return context -> "add-Clients".equals(context.getFormId()) && "submit".equals(context.getFormValue("action"));
    }

    @Override
    public void onActivity(FormReplyContext context) {
        final String name = context.getFormValue("portfolio");
        final String ticker = context.getFormValue("ticker");

        //Retrieve list of clients and corresponding amounts invested
        JsonNode node = context.getFormValues();
        HashMap<Long, Double> clients = new HashMap<Long, Double>();
        Iterator<java.util.Map.Entry<String, JsonNode>> fields = node.fields();
        while (fields.hasNext()) {
            java.util.Map.Entry<String, JsonNode> field = fields.next();
            try {
                clients.put(Long.parseLong(field.getKey()), field.getValue().asDouble());
            } catch (NumberFormatException e) {
            }

        }
        //Create portfolio and add it to the database
        final Portfolio p = new Portfolio(name, clients, ticker);
        Database.addPortfolio(context.getInitiator().getUser(), p);

        //Send confirmation message
        final String message = "<messageML>Created '" + name + "' Portfolio</messageML>";
        this.messageService.send(context.getSourceEvent().getStream(), Message.builder().content(message).build());

    }

    @Override
    protected ActivityInfo info() {
        return new ActivityInfo().type(ActivityType.FORM).name("Add Clients Initially")
                .description("\"Form handler that adds the initial clients\' monetary investments into the portfolio\"");
    }
}
