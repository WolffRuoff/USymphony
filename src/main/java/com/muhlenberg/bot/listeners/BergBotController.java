package com.muhlenberg.bot.listeners;

import com.muhlenberg.bot.Database;
import com.muhlenberg.bot.HelperSource;
import com.muhlenberg.bot.ObjectToContext;
import com.muhlenberg.models.*;

import com.symphony.bdk.core.service.message.MessageService;
import com.symphony.bdk.core.service.message.model.Message;
import com.symphony.bdk.gen.api.model.V4MessageSent;
import com.symphony.bdk.gen.api.model.V4User;
import com.symphony.bdk.spring.events.RealTimeEvent;
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
public class BergBotController {

  private final MessageService messageService;
  private Template template;
  private final TemplateLoader loader;
  private final Handlebars handlebars;

  public BergBotController(MessageService messageService) throws IOException {
    this.messageService = messageService;
    this.loader = new ClassPathTemplateLoader();
    loader.setPrefix("/templates");
    loader.setSuffix(".hbs");
    this.handlebars = new Handlebars(loader);
    this.template = handlebars.compile("help");
  }

  public void handleIncoming(RealTimeEvent<V4MessageSent> event, String[] msgParts) throws IOException {
    switch (msgParts[1]) {
      case "/help":
        onSlashHelp(event);
        break;
      case "/create":
        onSlashCreate(event);
        break;
      case "/buy":
        if (msgParts.length != 6 && msgParts.length != 2) {
          wrongArgs(event, "buy");
        } else {
          onSlashBuy(event, msgParts);
        }
        break;
      case "/blocktrade":
        onSlashBlockTrade(event);
        break;
      case "/portfolio":
        if (msgParts.length != 3 && msgParts.length != 2) {
          wrongArgs(event, "portfolio");
        } else {
          onSlashPortfolio(event, msgParts);
        }
        break;
      case "/summary":
        if (msgParts.length != 3) {
          wrongArgs(event, "summary");
        } else {
          onSlashSummary(event, msgParts);
        }
        break;
      case "/view":
        if (msgParts.length != 3 && msgParts.length != 2) {
          wrongArgs(event, "view");
        } else {
          onSlashView(event, msgParts);
        }
        break;
    }

  }

  public void wrongArgs(RealTimeEvent<V4MessageSent> event, String choice) {
    String message = "<messageML><div style=\"color:red;\">Wrong number of arguments. Please try again using the following: <br/>";
    if (choice.equals("buy")) {
      message = message
          + "@BergBot /buy &lt;Portfolio Name&gt; &lt;Ticker&gt; &lt;Amount or Shares&gt; &lt;Quantity&gt;</div></messageML>";
    } else if (choice.equals("portfolio")) {
      message = message + "@BergBot /portfolio &lt;Portfolio Name&gt;</div></messageML>";
    } else if (choice.equals("view")) {
      message = message + "@BergBot /view &lt;Portfolio Name&gt;</div></messageML>";
    } else if (choice.equals("summary")) {
      message = message + "@BergBot /summary &lt;Portfolio Name&gt;</div></messageML>";
    }
    this.messageService.send(event.getSource().getMessage().getStream(), Message.builder().content(message).build());
  }

  // Command to view the help list
  public void onSlashHelp(RealTimeEvent<V4MessageSent> context) {
    try {
      this.template = handlebars.compile("help");
      final String userEmail = context.getInitiator().getUser().getEmail();
      this.messageService.send(context.getSource().getMessage().getStream(), template.apply(userEmail));
    } catch (IOException e) {
      e.printStackTrace();
    }
  }

  // Command to create a new portfolio
  public void onSlashCreate(RealTimeEvent<V4MessageSent> context) {
    V4User user = context.getInitiator().getUser();

    try {
      this.template = handlebars.compile("create/createPortfolio");
      this.messageService.send(context.getSource().getMessage().getStream(), template.apply(user));
    } catch (JsonProcessingException e) {
      e.printStackTrace();
    } catch (IOException e1) {
      e1.printStackTrace();
    }
  }

  // Command to buy a new asset for the portfolio
  public void onSlashBuy(RealTimeEvent<V4MessageSent> context, String[] commandParts) throws IOException {
    V4User user = context.getInitiator().getUser();
    if (commandParts.length == 2) {
      // Gather list of portfolios belonging to the user and place in an object
      ArrayList<Portfolio> portfolioList = Database.getPortfolioList(user);
      SelectPortfolio portL = new SelectPortfolio("buy", portfolioList);

      // Try to display selectPortfolio message
      try {
        handlebars.registerHelpers(new HelperSource());
        this.template = handlebars.compile("selectPortfolio");
        Context c = ObjectToContext.Convert(portL);
        this.messageService.send(context.getSource().getMessage().getStream(), template.apply(c));
      } catch (JsonProcessingException e) {
        e.printStackTrace();
      } catch (IOException e1) {
        e1.printStackTrace();
      }
    }
    // Otherwise analyze parameters
    else {
      // 0 @BergBot
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
        }
        // Catch if ticker name is invalid
        catch (IOException | NullPointerException e) {
          final String message = "<messageML><div style=\"color:red;\">This is not a valid ticker. Please try again using &quot;@BergBot /buy &lt;Portfolio Name&gt; &lt;Ticker&gt; &lt;Amount or Shares&gt; &lt;Quantity&gt;&quot;</div></messageML>";
          this.messageService.send(context.getSource().getMessage().getStream(),
              Message.builder().content(message).build());
          return;
        }

        // Check if portName is invalid
        Portfolio p = Database.getPortfolio(user, portName, false);
        if (p == null) {
          final String message = "<messageML><div style=\"color:red;\"> This is not a valid portfolio. Please try again using &quot;@BergBot /buy &lt;Portfolio Name&gt; &lt;Ticker&gt; &lt;Amount or Shares&gt; &lt;Quantity&gt;&quot;</div></messageML>";
          this.messageService.send(context.getSource().getMessage().getStream(),
              Message.builder().content(message).build());
          return;
        }
        // Check sharesoramount and quantity
        try {
          if (ShareorAmount.equalsIgnoreCase("a")) {
            amount = Double.parseDouble(commandParts[5]);
            shares = amount / price;
          } else {
            shares = Double.parseDouble(commandParts[5]);
            amount = shares * price;
          }
        }
        // If number is invalid send error
        catch (NumberFormatException | NullPointerException e) {
          final String message = "<messageML><div style=\"color:red;\"> The amount is invalid. Please try again using &quot;@BergBot /buy &lt;Portfolio Name&gt; &lt;Ticker&gt; &lt;Amount or Shares&gt; &lt;Quantity&gt;&quot;</div></messageML>";
          this.messageService.send(context.getSource().getMessage().getStream(),
              Message.builder().content(message).build());
          return;
        }

        // check if the portfolio has enough liquid assets to buy shares
        Double liquidAmount = p.getSize() * p.getPortionLiquid();
        if (liquidAmount >= amount) {
          // Convert to object and send order confirmation
          OrderDetails orderDets = new OrderDetails(p, ticker, shares, price, amount);
          try {
            handlebars.registerHelpers(new HelperSource());
            template = handlebars.compile("buy/orderConfirmation");
            Context c = ObjectToContext.Convert(orderDets);
            this.messageService.send(context.getSource().getMessage().getStream(), template.apply(c));
          } catch (IOException e) {
            e.printStackTrace();
          }
        }

      }
      // Invalid selection of amount or share
      else {
        final String message = "<messageML><div style=\"color:red;\">This is not a valid option. Please select shares (s) or amount (a). Please try again using &quot;@BergBot /buy &lt;Portfolio Name&gt; &lt;Ticker&gt; &lt;Amount or Shares&gt; &lt;Quantity&gt;&quot;</div></messageML>";
        this.messageService.send(context.getSource().getMessage().getStream(),
            Message.builder().content(message).build());
        return;
      }
    }
  }

  // Command to Make a new block trade
  public void onSlashBlockTrade(RealTimeEvent<V4MessageSent> context) {
    V4User user = context.getInitiator().getUser();

    // Gather list of portfolios belonging to the user and place in an object
    ArrayList<Portfolio> portfolioList = Database.getPortfolioList(user);
    SelectPortfolio portL = new SelectPortfolio("blockTrade", portfolioList);

    try {
      this.template = handlebars.compile("blocktrade/blockTrade");
      Context c = ObjectToContext.Convert(portL);
      this.messageService.send(context.getSource().getMessage().getStream(), template.apply(c));
    } catch (JsonProcessingException e) {
      e.printStackTrace();
    } catch (IOException e1) {
      e1.printStackTrace();
    }
  }

  // Command to sell an asset for the portfolio
  /*
   * Under Development public void onSlashSell(CommandContext context) throws
   * IOException { V4User user = context.getInitiator().getUser(); String
   * commandParts[] = context.getTextContent().trim().split(" ");
   * 
   * if (commandParts.length == 2) { // Gather list of portfolios belonging to the
   * user and place in an object ArrayList<Portfolio> portfolioList =
   * Database.getPortfolioList(user); SelectPortfolio portL = new
   * SelectPortfolio("sell", portfolioList);
   * 
   * // Try to display selectPortfolio message try {
   * handlebars.registerHelpers(new HelperSource()); this.template =
   * handlebars.compile("selectPortfolio"); Context c =
   * ObjectToContext.Convert(portL);
   * this.messageService.send(context.getStreamId(), template.apply(c)); } catch
   * (JsonProcessingException e) { e.printStackTrace(); } catch (IOException e1) {
   * e1.printStackTrace(); } } // Otherwise analyze parameters else {
   * 
   * }
   * 
   * }
   */

  // Command to view a portfolio summary or client breakdown
  public void onSlashPortfolio(RealTimeEvent<V4MessageSent> context, String[] commandParts) {
    V4User user = context.getInitiator().getUser();

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
        this.messageService.send(context.getSource().getMessage().getStream(), template.apply(c));
      } catch (JsonProcessingException e) {
        e.printStackTrace();
      } catch (IOException e1) {
        e1.printStackTrace();
      }
    }
    // Otherwise analyze parameters
    else {
      // 0 @BergBot
      // 1 command
      // 2 portfolio name
      String portName = commandParts[2];
      Portfolio p = Database.getPortfolio(user, portName, false);

      // If the portfolio wasn't found
      if (p == null) {
        final String message = "<messageML><div style=\"color:red;\">'" + portName
            + "' doesn't exist or you are not authorized.</div></messageML>";
        this.messageService.send(context.getSource().getMessage().getStream(),
            Message.builder().content(message).build());
      } else {
        try {
          template = handlebars.compile("portfolio/clientBreakdownOrSummary");
          this.messageService.send(context.getSource().getMessage().getStream(), template.apply(portName));
        } catch (IOException e) {
          e.printStackTrace();
        }
      }
    }
  }

  // Command to view the summary of a portfolio
  public void onSlashSummary(RealTimeEvent<V4MessageSent> context, String[] commandParts) throws IOException {
    V4User user = context.getInitiator().getUser();
    String portName = commandParts[2];
    Portfolio p = Database.getPortfolio(user, portName, false);

    // Make sure portfolio exists
    if (p == null) {
      final String message = "<messageML><div style=\"color:red;\">'" + portName
          + "' doesn't exist or you are not authorized.</div></messageML>";
      this.messageService.send(context.getSource().getMessage().getStream(),
          Message.builder().content(message).build());
    } else {
      try {
        handlebars.registerHelpers(new HelperSource());
        this.template = handlebars.compile("portfolio/summary");
        Context c = ObjectToContext.Convert(new Summary(p));
        this.messageService.send(context.getSource().getMessage().getStream(), template.apply(c));
      } catch (JsonProcessingException e) {
        e.printStackTrace();
      }
    }
  }

  // Command for a client to view a portfolio summary
  public void onSlashView(RealTimeEvent<V4MessageSent> context, String[] commandParts) throws IOException {
    V4User user = context.getInitiator().getUser();

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
        this.messageService.send(context.getSource().getMessage().getStream(), template.apply(c));
      } catch (JsonProcessingException e) {
        e.printStackTrace();
      } catch (IOException e1) {
        e1.printStackTrace();
      }
    }
    // Otherwise analyze parameters
    else {
      // 0 @BergBot
      // 1 command
      // 2 portfolio name
        String portName = commandParts[2];
        ArrayList<Portfolio> portfolioList = Database.getPortfolioList(user);
        Portfolio p = Database.getPortfolio(user, portName, false);
        SelectPortfolio portL = new SelectPortfolio("view", portfolioList);
        if(p==null){
          final String message = "<messageML><div style=\"color:red;\">'" + portName
          + "' doesn't exist or you are not authorized.</div></messageML>";
          this.messageService.send(context.getSource().getMessage().getStream(), Message.builder().content(message).build());
          return;
        }
        try {
          handlebars.registerHelpers(new HelperSource());
          this.template = handlebars.compile("summary");
          Context c = ObjectToContext.Convert(portL);
          this.messageService.send(context.getSource().getMessage().getStream(), template.apply(c));
        } catch (JsonProcessingException e) {
          e.printStackTrace();
        }

      }


    }
  }
