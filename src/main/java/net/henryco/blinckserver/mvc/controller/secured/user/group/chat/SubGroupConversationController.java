package net.henryco.blinckserver.mvc.controller.secured.user.group.chat;

import net.henryco.blinckserver.configuration.project.notification.BlinckNotification;
import net.henryco.blinckserver.mvc.controller.BlinckController;
import net.henryco.blinckserver.mvc.service.infrastructure.UpdateNotificationService;
import net.henryco.blinckserver.mvc.service.relation.conversation.SubPartyConversationService;
import net.henryco.blinckserver.mvc.service.relation.core.SubPartyService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.security.access.AccessDeniedException;
import org.springframework.security.core.Authentication;
import org.springframework.stereotype.Component;
import org.springframework.web.bind.annotation.*;

import static org.springframework.http.HttpStatus.OK;
import static org.springframework.web.bind.annotation.RequestMethod.GET;
import static org.springframework.web.bind.annotation.RequestMethod.POST;
import static net.henryco.blinckserver.mvc.service.relation.conversation.SubPartyConversationService.SubPartyMessageForm;

@Component
final class SubGroupConversationServicePack {

	protected final SubPartyConversationService conversationService;
	protected final UpdateNotificationService notificationService;
	protected final SubPartyService subPartyService;

	@Autowired
	public SubGroupConversationServicePack(SubPartyConversationService conversationService,
										   UpdateNotificationService notificationService,
										   SubPartyService subPartyService) {
		this.conversationService = conversationService;
		this.notificationService = notificationService;
		this.subPartyService = subPartyService;
	}

	protected final void accessCheck(Long subPartyId, Long userId)
			throws AccessDeniedException{
		if (!subPartyService.isExistsWithUser(subPartyId, userId))
			throw new AccessDeniedException("Wrong user or conversation ID");
	}

	protected final void sendMessageNotification(Long subPartyId) {
		for (Long user : subPartyService.getSubPartyUsers(subPartyId)) {
			notificationService.addNotification(user,
					BlinckNotification.TYPE.SUB_PARTY_MESSAGE_REST,
					subPartyId.toString()
			);
		}
	}
}

/**
 * @author Henry on 18/09/17.
 */
@RestController // TODO: 18/09/17 Tests
@RequestMapping(BlinckController.EndpointAPI.SUB_GROUP_CONVERSATION)
public class SubGroupConversationController implements BlinckController {

	private final SubGroupConversationServicePack servicePack;

	@Autowired
	public SubGroupConversationController(SubGroupConversationServicePack servicePack) {
		this.servicePack = servicePack;
	}


	/*
	 *	SubGroup conversation API
	 *
	 *		ENDPOINT: 		/protected/user/subgroup/conversation
	 *
	 *
	 *	SubPartyMessageForm:
	 *
	 * 		"sub_party": 	LONG,
	 *		"author": 		LONG,
	 * 		"message": 		CHAR[512],
	 * 		"timestamp": 	DATE/LONG
	 *
	 *
	 *		COUNT:
	 *
	 *			ENDPOINT:	/messages/count
	 *			ARGS:		Long: id
	 *			METHOD:		GET
	 *			RETURN:		Long
	 *
	 *
	 *		LIST:
	 *
	 *			ENDPOINT:	/messages/list
	 *			ARGS:		Int: page, Int: size, Long: id
	 *			METHOD:		GET
	 *			RETURN:		SubPartyMessageForm[]
	 *
	 *
	 * 		SEND:
	 *
	 * 			ENDPOINT:	/messages/send
	 * 			BODY:		SubPartyMessageForm
	 * 			METHOD:		POST
	 * 			RETURN:		VOID
	 *
	 */


	public @RequestMapping(
			value = "/messages/count",
			method = GET
	) Long countMessages(Authentication authentication,
						 @RequestParam("id") Long subPartyId) {

		servicePack.accessCheck(subPartyId, longID(authentication));
		return servicePack.conversationService.countBySubPartyId(subPartyId);
	}


	/**
	 * <h1>SubParty Message JSON:</h1>
	 *	<h2>
	 * 	{&nbsp;
	 * 		"sub_party": 	LONG, 		&nbsp;
	 *		"author": 		LONG, 		&nbsp;
	 * 		"message": 		CHAR[512], 	&nbsp;
	 * 		"timestamp": 	DATE/LONG
	 *	&nbsp;}
	 *	</h2>
	 *	@author Henry on 18/09/17.
	 *	@see SubPartyMessageForm
	 */
	public @ResponseStatus(OK) @RequestMapping(
			value = "/messages/send",
			method = POST,
			consumes = JSON
	) void sendMessage(Authentication authentication,
					   @RequestBody SubPartyMessageForm messageForm) {

		servicePack.accessCheck(messageForm.getSubParty(), longID(authentication));
		servicePack.conversationService.sendMessage(messageForm);
		servicePack.sendMessageNotification(messageForm.getSubParty());
	}


	/**
	 * <h1>SubParty Message JSON:</h1>
	 *	<h2>
	 * 	{&nbsp;
	 * 		"sub_party": 	LONG, 		&nbsp;
	 *		"author": 		LONG, 		&nbsp;
	 * 		"message": 		CHAR[512], 	&nbsp;
	 * 		"timestamp": 	DATE/LONG
	 *	&nbsp;}
	 *	</h2>
	 *	@author Henry on 18/09/17.
	 *	@see SubPartyMessageForm
	 */
	public @RequestMapping(
			value = "/messages/list",
			method = GET,
			produces = JSON
	) SubPartyMessageForm[] getAllMessages(Authentication authentication,
										   @RequestParam("id") Long subPartyId,
										   @RequestParam("page") int page,
										   @RequestParam("size") int size) {

		servicePack.accessCheck(subPartyId, longID(authentication));
		return servicePack.conversationService.getLastNBySubPartyId(subPartyId, page, size);
	}

}