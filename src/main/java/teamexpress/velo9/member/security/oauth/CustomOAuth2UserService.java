package teamexpress.velo9.member.security.oauth;

import java.util.Collections;
import javax.servlet.http.HttpSession;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.security.core.authority.SimpleGrantedAuthority;
import org.springframework.security.oauth2.client.userinfo.DefaultOAuth2UserService;
import org.springframework.security.oauth2.client.userinfo.OAuth2UserRequest;
import org.springframework.security.oauth2.client.userinfo.OAuth2UserService;
import org.springframework.security.oauth2.core.OAuth2AuthenticationException;
import org.springframework.security.oauth2.core.user.DefaultOAuth2User;
import org.springframework.security.oauth2.core.user.OAuth2User;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import teamexpress.velo9.member.api.MemberThumbnailFileUploader;
import teamexpress.velo9.member.domain.Member;
import teamexpress.velo9.member.domain.MemberRepository;
import teamexpress.velo9.member.domain.MemberThumbnail;
import teamexpress.velo9.member.domain.MemberThumbnailRepository;
import teamexpress.velo9.member.domain.Role;
import teamexpress.velo9.member.dto.MemberThumbnailSaveDTO;

@Service
@Slf4j
@RequiredArgsConstructor
@Transactional(readOnly = true)
public class CustomOAuth2UserService implements OAuth2UserService<OAuth2UserRequest, OAuth2User> {

	private final MemberRepository memberRepository;
	private final MemberThumbnailRepository memberThumbnailRepository;
	private final HttpSession httpSession;
	private final MemberThumbnailFileUploader uploader;

	@Transactional
	@Override
	public OAuth2User loadUser(OAuth2UserRequest userRequest) throws OAuth2AuthenticationException {

		OAuth2UserService<OAuth2UserRequest, OAuth2User> delegate = new DefaultOAuth2UserService();
		OAuth2User oAuth2User = delegate.loadUser(userRequest);

		String registrationId = userRequest.getClientRegistration().getRegistrationId();
		String userNameAttributeName = userRequest
			.getClientRegistration()
			.getProviderDetails()
			.getUserInfoEndpoint()
			.getUserNameAttributeName();

		OAuthAttributes attributes = OAuthAttributes
			.of(registrationId,
				userNameAttributeName,
				oAuth2User.getAttributes());

		Member member = save(attributes);
		httpSession.setAttribute(SessionConst.LOGIN_MEMBER, member.getId());

		return new DefaultOAuth2User(
			Collections.singleton(new SimpleGrantedAuthority(member.getRoleKey())),
			attributes.getAttributes(),
			attributes.getNameAttributeKey());
	}

	private Member save(OAuthAttributes attributes) {
		checkEmail(attributes);

		MemberThumbnailSaveDTO memberThumbnailSaveDTO = uploader.upload(attributes.getPicture());
		MemberThumbnail memberThumbnail = memberThumbnailSaveDTO.toMemberThumbnail();

		memberThumbnailRepository.save(memberThumbnail);

		Member member = Member.builder()
			.nickname(attributes.getNickname())
			.email(attributes.getEmail())
			.role(Role.ROLE_USER)
			.memberThumbnail(memberThumbnail)
			.build();

		return memberRepository.save(member);
	}

	private void checkEmail(OAuthAttributes attributes) {
		memberRepository.findByEmail(attributes.getEmail())
			.ifPresent(m -> {
				throw new IllegalArgumentException("이미 존재하는 이메일입니다.");
			});
	}
}
