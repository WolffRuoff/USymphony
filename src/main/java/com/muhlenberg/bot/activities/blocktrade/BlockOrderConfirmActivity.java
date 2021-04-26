package com.muhlenberg.bot.activities.blocktrade;

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
import com.symphony.bdk.gen.api.model.V4User;

import org.springframework.stereotype.Component;

@Component
public class BlockOrderConfirmActivity extends FormReplyActivity<FormReplyContext> {

    private final MessageService messageService;

    public BlockOrderConfirmActivity(MessageService messageService) {
        this.messageService = messageService;
    }

    @Override
    public ActivityMatcher<FormReplyContext> matcher() {
        return context -> "blockOrderConfirm".equals(context.getFormId())
                && "submit".equals(context.getFormValue("action"));
    }

    @Override
    public void onActivity(FormReplyContext context) {
        V4User user = context.getInitiator().getUser();

        // Retrieve their choices
        String ticker = context.getFormValue("ticker");
        Double price = Double.parseDouble(context.getFormValue("price"));

        try {
            // Retrieve order information
            JsonNode node = context.getFormValues();
            Iterator<java.util.Map.Entry<String, JsonNode>> ports = node.fields();

            String portName;
            Portfolio p;
            Double purchaseAmount;
            Double thisShares;
            while (ports.hasNext()) {
                java.util.Map.Entry<String, JsonNode> portNode = ports.next();
                // Make sure entry is a portfolio
                if (portNode.getKey().charAt(0) == '9') {
                    // Retrieve portfolio, name, order percent, max percent
                    portName = portNode.getKey().substring(1);
                    purchaseAmount = Double.parseDouble(context.getFormValue("8" + portName));
                    if (purchaseAmount > 0.0) {
                        p = Database.getPortfolio(user, portName);
                        thisShares = purchaseAmount / price;
                        Database.placeOrder(user, p, ticker, thisShares, price, purchaseAmount);

                        // Send confirmation message
                        final String message = "<messageML><b>" + portName + "</b>: Purchased " + thisShares
                                + " shares of '" + ticker + "' for $" + purchaseAmount + ".</messageML>";
                        this.messageService.send(context.getSourceEvent().getStream(),
                                Message.builder().content(message).build());
                    }
                }
            }
        }
        // Send invalid ticker message and then blockTrade form again
        catch (NullPointerException e1) {
            e1.printStackTrace();
        }
    }

    @Override
    protected ActivityInfo info() {
        return new ActivityInfo().type(ActivityType.FORM).name("Block Order Confirm Activity")
                .description("\"Form handler for the blockOrderConfirm form\"");
    }

}