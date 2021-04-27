package com.muhlenberg.bot.listeners;

import com.muhlenberg.bot.Database;
import com.muhlenberg.bot.HelperSource;
import com.muhlenberg.bot.ObjectToContext;
import com.muhlenberg.models.*;

import com.symphony.bdk.core.activity.command.CommandContext;
import com.symphony.bdk.core.service.message.MessageService;
import com.symphony.bdk.gen.api.model.V4User;
import com.symphony.bdk.spring.annotation.Slash;
import com.fasterxml.jackson.core.JsonProcessingException;
import org.springframework.stereotype.Component;
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
    final String userEmail = context.getInitiator().getUser().getEmail();
    this.messageService.send(context.getStreamId(), this.template.apply(userEmail));
  }

  // Command to buy a new asset for the portfolio
  @Slash(value = "/buy", mentionBot = true)
  public void onSlashBuy(CommandContext context) throws IOException {
    V4User user = context.getInitiator().getUser();
    String commandParts[] = context.getTextContent().trim().split(" ");

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
