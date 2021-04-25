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
import com.muhlenberg.models.OrderDetails;
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

import yahoofinance.YahooFinance;

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
            Double percentOrdered;
            while (ports.hasNext()) {
                java.util.Map.Entry<String, JsonNode> portNode = ports.next();
                // Make sure entry is a portfolio
                if (portNode.getKey().charAt(0) == '9') {
                    //Retrieve portfolio, name, order percent,  max percent
                    portName = portNode.getKey().substring(1);
                    maxP = Double.parseDouble(context.getFormValue("8" + portName));
                    toOrder = portNode.getValue().asDouble();

                    //Add percent to total percent ordered
                    percentOrdered += toOrder;

                    //Retrieve Portfolio
                    Portfolio p = Database.getPortfolio(user, portName);

                    //Convert percents to dollar values
                    maxP = (maxP/100.0) * orderAmount;
                    toOrder = (toOrder/100) * orderAmount;

                    //check if amount is more that maxP (invalid)
                    if(toOrder > maxP) {orderedTooMuch = true;}

                    blockList.add(new BlockPortfolio(p, toOrder));
                }
            }

            // Create object for order details
            BlockOrderDetails orderDets = new BlockOrderDetails(portList, ticker, shares, price, orderAmount);
            // Make sure portfolios have enough liquid assets for the purchase
            if (orderDets.getTotalPurchasePower() >= orderAmount) {
                // Convert to context and send order confirmation
                handlebars.registerHelpers(new HelperSource());
                template = handlebars.compile("blockAllocation");
                Context c = ObjectToContext.Convert(orderDets);
                this.messageService.send(context.getSourceEvent().getStream(), template.apply(c));
            } else {
                try {
                    final String message = "<messageML>These portfolios only have $" + orderDets.getTotalPurchasePower()
                            + " of liquid assets. <br/> Please lower the order amount or add more portfolios.</messageML>";
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