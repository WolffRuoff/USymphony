package com.muhlenberg.bot;

import com.symphony.bdk.core.activity.command.CommandContext;
import com.symphony.bdk.core.service.message.MessageService;
import com.symphony.bdk.core.service.message.model.Message;
import com.symphony.bdk.spring.annotation.Slash;
import com.symphony.bdk.template.api.Template;

import org.springframework.stereotype.Component;
import static java.util.Collections.singletonMap;

@Component
public class HelpSlashHandler {

  private final MessageService messageService;
  private final Template template;

  public HelpSlashHandler(MessageService messageService) {
    this.messageService = messageService;
    this.template = messageService.templates().newTemplateFromClasspath("/templates/help.ftl");
  }

  @Slash(value = "/help", mentionBot = true)
  public void onSlashHelp(CommandContext context) {
    final String userEmail = context.getInitiator().getUser().getEmail();
    this.messageService.send(context.getStreamId(), Message.builder().template(this.template, singletonMap("name", userEmail)).build());
  }
}
