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
public class selectBlockPortfoliosActivity extends FormReplyActivity<FormReplyContext> {

    private final MessageService messageService;

    public selectBlockPortfoliosActivity(MessageService messageService) {
        this.messageService = messageService;
    }

    @Override
    public ActivityMatcher<FormReplyContext> matcher() {
        return context -> "selectBlockPortfolios".equals(context.getFormId())
                && "blockTrade".equals(context.getFormValue("action"));
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

        // Retrieve their choices and the portfolios they chose
        String sharesOrPrice = context.getFormValue("buyOptions");
        String ticker = context.getFormValue("ticker");

        // Retrieve the ticker price
        Double price = 1.00;
        Double shares = 1.00;
        Double orderAmount = 1.00;
        try {
            price = YahooFinance.get(ticker).getQuote().getPrice().doubleValue();

            // Convert shares to dollar value
            if (sharesOrPrice.equals("shares")) {
                shares = Double.parseDouble(context.getFormValue("Amount"));
                orderAmount = shares * price;
            }
            // Convert dollar value to shares
            else {
                orderAmount = Double.parseDouble(context.getFormValue("Amount"));
                shares = orderAmount / price;
            }

            // Retrieve portfolios checked
            JsonNode node = context.getFormValues();
            ArrayList<Portfolio> portList = new ArrayList<Portfolio>();
            Iterator<java.util.Map.Entry<String, JsonNode>> ports = node.fields();
            while (ports.hasNext()) {
                java.util.Map.Entry<String, JsonNode> portNode = ports.next();
                // Make sure entry is a portfolio
                if (portNode.getKey().charAt(0) == '9') {
                    portList.add(Database.getPortfolio(user, portNode.getValue().asText()));
                }
            }

            // Create object for order details
            BlockOrderDetails orderDets = new BlockOrderDetails(portList, ticker, shares, price, orderAmount);
            //Make sure portfolios have enough liquid assets for the purchase
            if (orderDets.getTotalPurchasePower() >= orderAmount) {
                // Convert to context and send order confirmation
                handlebars.registerHelpers(new HelperSource());
                template = handlebars.compile("blockAllocation");
                Context c = ObjectToContext.Convert(orderDets);
                this.messageService.send(context.getSourceEvent().getStream(), template.apply(c));
            }
            else {
                try {
                    final String message = "<messageML>These portfolios only have $" + orderDets.getTotalPurchasePower() + " of liquid assets. <br/> Please lower the order amount or add more portfolios.</messageML>";
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
        return new ActivityInfo().type(ActivityType.FORM).name("Name of the Portfolio")
                .description("\"Form handler for the buyActivity form\"");
    }

}