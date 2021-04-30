package com.muhlenberg.bot.listeners;

import com.muhlenberg.bot.Database;
import com.muhlenberg.bot.HelperSource;
import com.muhlenberg.bot.ObjectToContext;
import com.muhlenberg.models.*;

import com.symphony.bdk.core.activity.command.CommandContext;
import com.symphony.bdk.core.service.message.MessageService;
import com.symphony.bdk.core.service.message.model.Message;
import com.symphony.bdk.gen.api.model.V4User;
import com.symphony.bdk.spring.annotation.Slash;
import com.fasterxml.jackson.core.JsonProcessingException;

import org.springframework.stereotype.Component;

import yahoofinance.YahooFinance;

import java.io.IOException;
import java.util.ArrayList;

import com.github.jknack.handlebars.Template;
import com.github.jknack.handlebars.Handlebars;
import com.github.jknack.handlebars.Context;
import com.github.jknack.handlebars.io.ClassPathTemplateLoader;
import com.github.jknack.handlebars.io.TemplateLoader;

@Component
public class SlashHandler {

  private final MessageService messageService;
  private Template template;
  private final TemplateLoader loader;
  private final Handlebars handlebars;

  public SlashHandler(MessageService messageService) throws IOException {
    this.messageService = messageService;
    this.loader = new ClassPathTemplateLoader();
    loader.setPrefix("/templates");
    loader.setSuffix(".hbs");
    this.handlebars = new Handlebars(loader);
    this.template = handlebars.compile("help");
  }

  // Command to view the help list
  @Slash(value = "/help", mentionBot = true)
  public void onSlashHelp(CommandContext context) throws IOException {
    this.template = handlebars.compile("help");
    final String userEmail = context.getInitiator().getUser().getEmail();
    this.messageService.send(context.getStreamId(), template.apply(userEmail));
  }

  // Command to buy a new asset for the portfolio
  @Slash(value = "/buy", mentionBot = true)
  public void onSlashBuy(CommandContext context) throws IOException {
    V4User user = context.getInitiator().getUser();
    String[] commandParts = context.getTextContent().trim().split("\\s+");
    if (commandParts.length == 2) {
      // Gather list of portfolios belonging to the user and place in an object
      ArrayList<Portfolio> portfolioList = Database.getPortfolioList(user);
      SelectPortfolio portL = new SelectPortfolio("buy", portfolioList);

      // Try to display selectPortfolio message
      try {
        handlebars.registerHelpers(new HelperSource());
        this.template = handlebars.compile("selectPortfolio");
        Context c = ObjectToContext.Convert(portL);
        this.messageService.send(context.getStreamId(), template.apply(c));
      } catch (JsonProcessingException e) {
        e.printStackTrace();
      } catch (IOException e1) {
        e1.printStackTrace();
      }
    }
    // Otherwise analyze parameters
    else {
      // check what they sent
      // commands parts 0 bergbot
      // 1 command
      // 2 portfolio name
      // 3 ticker
      // 4 shares or amount
      // 5 quanitity
      if (commandParts[4].equalsIgnoreCase("a") || commandParts[4].equalsIgnoreCase("s")) {
        String ShareorAmount = commandParts[4];// saves s or a option
        String ticker = commandParts[3];// saves ticker
        Double amount = 0.0;
        Double price = 0.00;
        Double shares = 0.00;
        String portName = commandParts[2];

        // Check if ticker is valid
        try {
          price = YahooFinance.get(ticker).getQuote().getPrice().doubleValue();

        } catch (IOException e) {// invalid ticker name
          final String message = "<messageML><div style=\"color:red;\">'"
              + "' This is not a valid ticker. /buy portolio, a or s, double" + ". Please try again.</div></messageML>";
          this.messageService.send(context.getStreamId(), Message.builder().content(message).build());
          return;
        }

        // Check if portName is invalid
        Portfolio p = Database.getPortfolio(user, portName, false);
        if (p == null) {
          final String message = "<messageML><div style=\"color:red;\"> This is not a valid portfolio"
              + ". Please try again.</div></messageML>";
          this.messageService.send(context.getStreamId(), Message.builder().content(message).build());
          return;
        }
        // Check sharesoramount and quantity
        try {
          if (ShareorAmount.equalsIgnoreCase("a")) {
            amount = Double.parseDouble(commandParts[4]);
            shares = amount / price; // calc the number of shares you own
          } else {
            shares = Double.parseDouble(commandParts[4]);
            amount = shares * price;
          }
        } catch (NumberFormatException | NullPointerException e) {// invalid number
          final String message = "<messageML><div style=\"color:red;\"> The amount is invalid. @BerbBot /buy <portolio name> <ticker> <amount or shares (a or s)> <quantity>, a or s, double"
              + ". Please try again.</div></messageML>";
          this.messageService.send(context.getStreamId(), Message.builder().content(message).build());
          return;
        }
        // check if portfolio has enough liquid assets to buy shares
        Double liquidAmount = p.getSize() * p.getPortionLiquid();
        if (liquidAmount >= amount) {
          // Convert to object and send order confirmation
          OrderDetails orderDets = new OrderDetails(p, ticker, shares, price, amount);
          try {
            template = handlebars.compile("orderConfirmation");
            Context c = ObjectToContext.Convert(orderDets);
            this.messageService.send(context.getStreamId(), template.apply(c));
          } catch (IOException e) {
            e.printStackTrace();
          }
        }

      } else {// invalid selection of amount or share
        final String message = "<messageML><div style=\"color:red;\">'"
            + "' This is not a valid option please select shares or amount. /buy portolio, a or s, double"
            + ". Please try again.</div></messageML>";
        this.messageService.send(context.getStreamId(), Message.builder().content(message).build());
        return;
      }
    }
  }

  // Command to sell an asset for the portfolio
  @Slash(value = "/sell", mentionBot = true)
  public void onSlashSell(CommandContext context) throws IOException {
    V4User user = context.getInitiator().getUser();
    String commandParts[] = context.getTextContent().trim().split(" ");

    if (commandParts.length == 2) {
      // Gather list of portfolios belonging to the user and place in an object
      ArrayList<Portfolio> portfolioList = Database.getPortfolioList(user);
      SelectPortfolio portL = new SelectPortfolio("sell", portfolioList);

      // Try to display selectPortfolio message
      try {
        handlebars.registerHelpers(new HelperSource());
        this.template = handlebars.compile("selectPortfolio");
        Context c = ObjectToContext.Convert(portL);
        this.messageService.send(context.getStreamId(), template.apply(c));
      } catch (JsonProcessingException e) {
        e.printStackTrace();
      } catch (IOException e1) {
        e1.printStackTrace();
      }
    }
    // Otherwise analyze parameters
    else {

    }

  }

  // Command to create a new portfolio
  @Slash(value = "/create", mentionBot = true)
  public void onSlashCreate(CommandContext context) {
    V4User user = context.getInitiator().getUser();

    try {
      this.template = handlebars.compile("create/createPortfolio");
      this.messageService.send(context.getStreamId(), template.apply(user));
    } catch (JsonProcessingException e) {
      e.printStackTrace();
    } catch (IOException e1) {
      e1.printStackTrace();
    }
  }
  
  // Command to view a portfolio summary or client breakdown
  @Slash(value = "/portfolio", mentionBot = true)
  public void onSlashPortfolio(CommandContext context) {
    V4User user = context.getInitiator().getUser();
    String commandParts[] = context.getTextContent().trim().split(" ");
    String portName = commandParts[2];

    // If command is just /portfolio display form
    if (commandParts.length == 2) {
      // Gather list of portfolios belonging to the user and place in an object
      ArrayList<Portfolio> portfolioList = Database.getPortfolioList(user);
      SelectPortfolio portL = new SelectPortfolio("view", portfolioList);

      // Try to display selectPortfolio message
      try {
        handlebars.registerHelpers(new HelperSource());
        this.template = handlebars.compile("selectPortfolio");
        Context c = ObjectToContext.Convert(portL);
        this.messageService.send(context.getStreamId(), template.apply(c));
      } catch (JsonProcessingException e) {
        e.printStackTrace();
      } catch (IOException e1) {
        e1.printStackTrace();
      }
    }
    // Otherwise analyze parameters
    else {
      Portfolio p = Database.getPortfolio(user, portName, false);
      if (p == null) {//if null p 
        final String message = "<messageML><div style=\"color:red;\">'" + portName
            + "' doesn't exist or you are not authorized.</div></messageML>";
            this.messageService.send(context.getStreamId(), Message.builder().content(message).build());
            return;
      }
      try {
        template = handlebars.compile("summary");
        Context c = ObjectToContext.Convert(new Summary(p));
        this.messageService.send(context.getStreamId(), template.apply(c));
      } catch (IOException e) {
        e.printStackTrace();
      }

    }

  }

  // Command to create a new portfolio
  @Slash(value = "/summary", mentionBot = true)
  public void onSlashSummary(CommandContext context) throws IOException{
    V4User user = context.getInitiator().getUser();
    String commandParts[] = context.getTextContent().trim().split(" ");
    String portName = commandParts[2];
    if (commandParts.length == 2) {
      // Gather list of portfolios belonging to the user and place in an object
      ArrayList<Portfolio> portfolioList = Database.getPortfolioList(user);
      SelectPortfolio portL = new SelectPortfolio("view", portfolioList);
      Portfolio p = Database.getPortfolio(user, portName, false);
    if(p==null){
      final String message = "<messageML><div style=\"color:red;\">'" + portName
      + "' doesn't exist or you are not authorized.</div></messageML>";
      this.messageService.send(context.getStreamId(), Message.builder().content(message).build());
      return;
    }

    
    try {
      handlebars.registerHelpers(new HelperSource());
      this.template = handlebars.compile("summary");
      Context c = ObjectToContext.Convert(portL);
      this.messageService.send(context.getStreamId(), template.apply(c));
    } catch (JsonProcessingException e) {
      e.printStackTrace();
    }
  }
}
  // Command for a client to view a portfolio summary
  @Slash(value = "/view", mentionBot = true)
  public void onSlashView(CommandContext context) {
    V4User user = context.getInitiator().getUser();
    String commandParts[] = context.getTextContent().trim().split(" ");
    // If command is just /portfolio display form
    if (commandParts.length == 2) {
      // Gather list of portfolios belonging to the user and place in an object
      ArrayList<Portfolio> portfolioList = Database.getClientPortfolioList(user);
      SelectPortfolio portL = new SelectPortfolio("client", portfolioList);

      // Try to display selectPortfolio message
      try {
        handlebars.registerHelpers(new HelperSource());
        this.template = handlebars.compile("selectPortfolio");
        Context c = ObjectToContext.Convert(portL);
        this.messageService.send(context.getStreamId(), template.apply(c));
      } catch (JsonProcessingException e) {
        e.printStackTrace();
      } catch (IOException e1) {
        e1.printStackTrace();
      }
    }
    // Otherwise analyze parameters
    else {

    }

  }

  // Command to Make a new block trade
  @Slash(value = "/blocktrade", mentionBot = true)
  public void onSlashBlockTrade(CommandContext context) {
    V4User user = context.getInitiator().getUser();

    // Gather list of portfolios belonging to the user and place in an object
    ArrayList<Portfolio> portfolioList = Database.getPortfolioList(user);
    SelectPortfolio portL = new SelectPortfolio("blockTrade", portfolioList);

    try {
      this.template = handlebars.compile("blocktrade/blockTrade");
      Context c = ObjectToContext.Convert(portL);
      this.messageService.send(context.getStreamId(), template.apply(c));
    } catch (JsonProcessingException e) {
      e.printStackTrace();
    } catch (IOException e1) {
      e1.printStackTrace();
    }
  }
}
