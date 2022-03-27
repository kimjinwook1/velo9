package teamexpress.velo9.member.service;

import lombok.RequiredArgsConstructor;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import teamexpress.velo9.member.domain.Member;
import teamexpress.velo9.member.domain.MemberRepository;
import teamexpress.velo9.member.domain.MemberThumbnail;
import teamexpress.velo9.member.dto.MailDTO;
import teamexpress.velo9.member.dto.MemberDTO;
import teamexpress.velo9.member.dto.MemberEditDTO;
import teamexpress.velo9.member.dto.MemberSignupDTO;
import teamexpress.velo9.member.dto.MemberThumbnailDTO;
import teamexpress.velo9.member.dto.PasswordDTO;
import teamexpress.velo9.member.dto.SocialSignupDTO;

@RequiredArgsConstructor
@Transactional(readOnly = true)
@Service
public class MemberService {

	private final MemberRepository memberRepository;
	private final PasswordEncoder passwordEncoder;

	@Transactional
	public void join(MemberSignupDTO signupDTO) {
		checkDuplicateMember(signupDTO.getUsername(), signupDTO.getNickname());
		encodePassword(signupDTO);
		Member member = signupDTO.toMember();
		memberRepository.save(member);
	}

	@Transactional
	public MemberDTO editMember(Long memberId, MemberEditDTO memberEditDTO) {
		Member findMember = getMember(memberId);
		Member editMember = changeMemberInfo(memberEditDTO, findMember);
		return new MemberDTO(editMember);
	}

	@Transactional
	public void changePassword(Long memberId, PasswordDTO passwordDTO) {
		Member findMember = checkPasswordMember(passwordDTO, memberId);
		String encodedPassword = passwordEncoder.encode(passwordDTO.getNewPassword());
		findMember.changePassword(encodedPassword);
	}

	@Transactional
	public void withdraw(Long memberId, PasswordDTO passwordDTO) {
		Member findMember = checkPasswordMember(passwordDTO, memberId);
		memberRepository.delete(findMember);
	}

	@Transactional
	public void uploadThumbnail(MemberThumbnailDTO memberThumbnailDTO, Long memberId) {
		Member member = memberRepository.findById(memberId)
			.orElseThrow(() -> new NullPointerException());

		MemberThumbnail memberThumbnail = member.getMemberThumbnail();

		if (memberThumbnail != null) {
			memberThumbnailDTO.setId(memberThumbnail.getId());
		}

		member.uploadThumbnail(memberThumbnailDTO.toMemberThumbnail());

		memberRepository.save(member);
	}

	@Transactional
	public void deleteThumbnail(Long memberId) {
		Member member = memberRepository.findById(memberId)
			.orElseThrow(() -> new NullPointerException());

		if (member.getMemberThumbnail() == null) {
			return;
		}

		member.uploadThumbnail(null);

		memberRepository.save(member);
	}

	public void findEmail(MailDTO mailDTO) {
		memberRepository.findByEmail(mailDTO.getEmail())
			.ifPresent(m -> {
				throw new IllegalArgumentException("이미 존재하는 이메일입니다.");
			});
	}

	public Member joinSocial(SocialSignupDTO socialSignupDTO, Long memberId) {

		Member member = getMember(memberId);
		checkDuplicateMember(socialSignupDTO.getUsername(), socialSignupDTO.getNickname());
		String encodePassword = passwordEncoder.encode(socialSignupDTO.getPassword());
		member.registerSocialMember(
			socialSignupDTO.getUsername(),
			encodePassword,
			socialSignupDTO.getNickname());

		return member;
	}

	public boolean checkSignup(Member joinMember) {
		return checkRegister(joinMember);
	}

	private Member getMember(Long memberId) {
		return memberRepository.findById(memberId).orElseThrow(() -> {
			throw new IllegalArgumentException("존재하지 않는 회원입니다.");
		});
	}

	private void encodePassword(MemberSignupDTO signupDTO) {
		signupDTO.setPassword(passwordEncoder.encode(signupDTO.getPassword()));
	}

	private void checkDuplicateMember(String username, String nickname) {
		validateUsername(username);
		validateNickname(nickname);
	}

	private void validateUsername(String username) {
		memberRepository.findByUsername(username)
			.ifPresent(m -> {
				throw new IllegalArgumentException("이미 존재하는 아이디입니다.");
			});
	}

	private void validateNickname(String nickname) {
		memberRepository.findByNickname(nickname)
			.ifPresent(m -> {
				throw new IllegalArgumentException("이미 존재하는 닉네임입니다.");
			});
	}

	private Member changeMemberInfo(MemberEditDTO memberEditDTO, Member findMember) {
		return findMember.edit(
			memberEditDTO.getNickname(), memberEditDTO.getIntroduce(),
			memberEditDTO.getBlogTitle(), memberEditDTO.getSocialEmail(),
			memberEditDTO.getSocialGithub());
	}

	private Member checkPasswordMember(PasswordDTO passwordDTO, Long memberId) {
		Member findMember = getMember(memberId);
		checkPassword(passwordDTO.getOldPassword(), findMember.getPassword());
		return findMember;
	}

	private void checkPassword(String oldPassword, String savedPassword) {
		if (!passwordEncoder.matches(oldPassword, savedPassword)) {
			throw new IllegalArgumentException("잘못된 비밀번호 입니다.");
		}
	}

	private boolean checkRegister(Member member) {
		if (member.getUsername() == null || member.getPassword() == null || member.getNickname() == null) {
			memberRepository.delete(member);
			return false;
		}
		return true;
	}
}
