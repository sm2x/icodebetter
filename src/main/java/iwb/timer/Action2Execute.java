package iwb.timer;

import java.util.Map;

import iwb.model.db.Log5Notification;
import iwb.model.db.W5Email;
import iwb.model.helper.W5QueuedActionHelper;
import iwb.util.GenericUtil;
import iwb.util.MailUtil;
import iwb.util.UserUtil;

public class Action2Execute implements Runnable {// TODO: buralar long polling ile olacak
	private W5QueuedActionHelper queuedAction;
	private Map<String, Object> scd;

	@Override
	public void run() {
		Log5Notification n = new Log5Notification();// sanal
		n.setUserId((Integer)scd.get("userId"));
		n.setUserTip((short) GenericUtil.uInt(scd.get("userTip")));
		try {
			if(queuedAction.getAction() instanceof W5Email){
				W5Email email = (W5Email)queuedAction.getAction();
				String error = MailUtil.sendMail(scd, email);
				if(GenericUtil.isEmpty(error)){
					n.setNotificationTip((short) 1);// success
					n.set_tmpStr("<b>Email Successfully Sent:</b>" + email.getMailSubject());
				} else {
					n.setNotificationTip((short) 3);// exec-basarisiz: atiyorum
					n.set_tmpStr("<b>Email Could not Sent:</b>" + email.getMailSubject());
				}
			}
		} catch (Exception e) {
			n.setNotificationTip((short) 3);// exec-basarisiz: atiyorum
			n.set_tmpStr(e.getMessage());
		}

		UserUtil.publishNotification(n, false);
	}

	public Action2Execute(W5QueuedActionHelper qa, Map<String, Object> scd) {
		this.queuedAction = qa;
		this.scd = scd;
	}
}