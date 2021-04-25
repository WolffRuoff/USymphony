package com.muhlenberg.bot.activities;

import java.util.HashMap;
import java.util.Iterator;
import java.util.Map.Entry;

import com.fasterxml.jackson.databind.JsonNode;
import com.muhlenberg.bot.Database;
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
        HashMap<Long, Double> clientsAm = new HashMap<Long, Double>();
        Iterator<java.util.Map.Entry<String, JsonNode>> fields = node.fields();
        float size = 0;
        while (fields.hasNext()) {
            java.util.Map.Entry<String, JsonNode> field = fields.next();
            //Makes it only add client numbers and ignore other fields
            try {
                size += field.getValue().asDouble();
                clientsAm.put(Long.parseLong(field.getKey()), field.getValue().asDouble());
            } catch (NumberFormatException e) {
            }
        }

        //Convert amounts to percentages
        HashMap<Long, Double> clients = new HashMap<Long, Double>();
        for(Entry<Long, Double> entry : clientsAm.entrySet()) {
            clients.put(entry.getKey(),entry.getValue()/size);
        }

        //Create portfolio and add it to the database
        final Portfolio p = new Portfolio(name, size, 1.0D, clients, new HashMap<Stock, Double>(), ticker);
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
