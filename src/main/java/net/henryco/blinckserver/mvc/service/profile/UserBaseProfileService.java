package net.henryco.blinckserver.mvc.service.profile;

import net.henryco.blinckserver.mvc.model.dao.profile.UserCoreProfileDao;
import net.henryco.blinckserver.mvc.model.entity.profile.UserCoreProfile;
import net.henryco.blinckserver.util.dao.BlinckDaoProvider;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

/**
 * @author Henry on 23/08/17.
 */
@Service
public class UserBaseProfileService extends BlinckDaoProvider<UserCoreProfile, Long> {


	@Autowired
	public UserBaseProfileService(UserCoreProfileDao baseProfileDao) {
		super (((baseProfileDao))) ;
	}

	private UserCoreProfileDao getDao() {
		return provideDao();
	}

	@Transactional
	public String getGender(Long userId) {
		return getDao().getById(userId).getPublicProfile().getGender();
	}

}