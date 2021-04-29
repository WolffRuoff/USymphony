package com.muhlenberg.bot.activities.blocktrade;

import java.io.IOException;
import java.util.ArrayList;
import java.util.Iterator;

import com.fasterxml.jackson.databind.JsonNode;
import com.github.jknack.handlebars.Context;
import com.github.jknack.handlebars.Handlebars;
import com.github.jknack.handlebars.Template;
import com.github.jknack.handlebars.io.ClassPathTemplateLoader;
import com.github.jknack.handlebars.io.TemplateLoader;
import com.muhlenberg.bot.Database;
import com.muhlenberg.bot.HelperSource;
import com.muhlenberg.bot.ObjectToContext;
import com.muhlenberg.models.BlockOrderDetails;
import com.muhlenberg.models.BlockPortfolio;
import com.muhlenberg.models.Portfolio;
import com.muhlenberg.models.SelectPortfolio;
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
public class BlockAllocateActivity extends FormReplyActivity<FormReplyContext> {

    private final MessageService messageService;

    public BlockAllocateActivity(MessageService messageService) {
        this.messageService = messageService;
    }

    @Override
    public ActivityMatcher<FormReplyContext> matcher() {
        return context -> "blockAllocate".equals(context.getFormId())
                && "submit".equals(context.getFormValue("action"));
    }

    @Override
    public void onActivity(FormReplyContext context) {
        V4User user = context.getInitiator().getUser();
        // Load handlebars stuff
        TemplateLoader loader = new ClassPathTemplateLoader();
        loader.setPrefix("/templates/blocktrade");
        loader.setSuffix(".hbs");
        Handlebars handlebars = new Handlebars(loader);
        handlebars.registerHelpers(new HelperSource());
        Template template;

        // Retrieve their choices
        String ticker = context.getFormValue("ticker");
        Double shares = Double.parseDouble(context.getFormValue("shares"));
        Double price = Double.parseDouble(context.getFormValue("price"));
        Double orderAmount = Double.parseDouble(context.getFormValue("orderAmount"));

        try {
            // Retrieve order information
            JsonNode node = context.getFormValues();
            boolean orderedTooMuch = false;
            ArrayList<BlockPortfolio> blockList = new ArrayList<BlockPortfolio>();
            Iterator<java.util.Map.Entry<String, JsonNode>> ports = node.fields();

            String portName;
            Double maxP;
            Double toOrder;
            Double percentOrdered = 0.0; // Total percent allocated
            Double emptyCount = 0.0;
            while (ports.hasNext()) {
                java.util.Map.Entry<String, JsonNode> portNode = ports.next();
                // Make sure entry is a portfolio
                if (portNode.getKey().charAt(0) == '9') {
                    // Retrieve portfolio, name, order percent, max percent
                    portName = portNode.getKey().substring(1);
                    maxP = Double.parseDouble(context.getFormValue("8" + portName));
                    // Check if amount is empty
                    if (portNode.getValue().asText().isEmpty()) {
                        toOrder = -1.0;
                        emptyCount++;
                    } else {
                        toOrder = portNode.getValue().asDouble();

                        // Add percent to total percent ordered
                        percentOrdered += toOrder;

                        // Convert percents to dollar values
                        maxP = (maxP / 100.0) * orderAmount;
                        toOrder = (toOrder / 100.0) * orderAmount;

                        // check if amount is more that maxP (invalid)
                        if (toOrder > maxP) {
                            orderedTooMuch = true;
                        }
                    }
                    // Retrieve Portfolio
                    Portfolio p = Database.getPortfolio(user, portName, false);

                    blockList.add(new BlockPortfolio(p, toOrder));
                }
            }

            // If emptyCount > 0 add remaining allocation evenly to them
            if (emptyCount > 0 && !orderedTooMuch && percentOrdered <= 100.0) {
                Double alAmount = (((100.0 - percentOrdered) / 100.0) * orderAmount) / emptyCount;

                for (BlockPortfolio block : blockList) {
                    if (block.getPercent() < 0.0) {
                        maxP = (Double.parseDouble(context.getFormValue("8" + block.getName())) / 100.0) * orderAmount;
                        // check if amount is more that maxP (invalid)
                        if (maxP < alAmount) {
                            orderedTooMuch = true;
                        }
                        block.setPercent(alAmount);
                    }
                }
            }

            // Check if they ordered too much
            if (orderedTooMuch || percentOrdered > 100.0) {
                // Resend Allocation form with error
                ArrayList<Portfolio> portList = new ArrayList<Portfolio>();
                for (BlockPortfolio block : blockList) {
                    portList.add(block.getPortfolio());
                }
                BlockOrderDetails orderDets = new BlockOrderDetails(portList, ticker, shares, price, orderAmount);

                final String message = "<messageML><div style=\"color: red;\">You ordered too much for at least one portfolio!</div></messageML>";
                this.messageService.send(context.getSourceEvent().getStream(),
                        Message.builder().content(message).build());
                template = handlebars.compile("blockAllocation");
                Context c = ObjectToContext.Convert(orderDets);
                this.messageService.send(context.getSourceEvent().getStream(), template.apply(c));
            }
            // Create and send the order confirmation!
            else {
                // Create object for order details
                BlockOrderDetails orderDets = new BlockOrderDetails(blockList, ticker, shares, price, orderAmount, 0);
                template = handlebars.compile("blockOrderConfirmation");
                Context c = ObjectToContext.Convert(orderDets);
                this.messageService.send(context.getSourceEvent().getStream(), template.apply(c));
            }
        }

        // Send invalid ticker message and then blockTrade form again
        catch (IOException | NullPointerException e1) {
            try {
                final String message = "<messageML>'" + ticker + "' name was invalid. Please try again.</messageML>";
                this.messageService.send(context.getSourceEvent().getStream(),
                        Message.builder().content(message).build());

                template = handlebars.compile("blockTrade");
                ArrayList<Portfolio> portfolioList = Database.getPortfolioList(user);
                SelectPortfolio portL = new SelectPortfolio("blockTrade", portfolioList);
                Context c = ObjectToContext.Convert(portL);
                this.messageService.send(context.getSourceEvent().getStream(), template.apply(c));
            } catch (IOException e) {
                e.printStackTrace();
            }
            e1.printStackTrace();
        }
    }

    @Override
    protected ActivityInfo info() {
        return new ActivityInfo().type(ActivityType.FORM).name("Block Allocate Activity")
                .description("\"Form handler for the blockAllocate form\"");
    }

}