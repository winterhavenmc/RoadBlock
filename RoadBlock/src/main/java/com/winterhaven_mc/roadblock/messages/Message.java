package com.winterhaven_mc.roadblock.messages;

import com.winterhaven_mc.util.AbstractMessage;
import org.bukkit.command.CommandSender;


public class Message extends AbstractMessage<MessageId, Macro> {


	/**
	 * Private class constructor; calls inherited super constructor
	 *
	 * @param recipient the message recipient
	 * @param messageId the enum entry representing the message to be displayed
	 */
	private Message(CommandSender recipient, MessageId messageId) {
		super(recipient, messageId);
	}


	/**
	 * Static class constructor
	 *
	 * @param recipient the message recipient
	 * @param messageId the enum entry representing the message to be displayed
	 * @return new instance of Message created with private constructor
	 */
	public static Message create(CommandSender recipient, MessageId messageId) {
		return new Message(recipient, messageId);
	}

}
