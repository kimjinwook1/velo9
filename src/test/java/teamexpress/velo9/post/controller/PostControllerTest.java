package teamexpress.velo9.post.controller;

import static org.springframework.restdocs.mockmvc.MockMvcRestDocumentation.document;
import static org.springframework.restdocs.payload.PayloadDocumentation.fieldWithPath;
import static org.springframework.restdocs.payload.PayloadDocumentation.relaxedRequestFields;
import static org.springframework.restdocs.payload.PayloadDocumentation.relaxedResponseFields;
import static org.springframework.restdocs.payload.PayloadDocumentation.requestFields;
import static org.springframework.restdocs.payload.PayloadDocumentation.responseFields;
import static org.springframework.restdocs.request.RequestDocumentation.parameterWithName;
import static org.springframework.restdocs.request.RequestDocumentation.pathParameters;
import static org.springframework.restdocs.request.RequestDocumentation.requestParameters;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.get;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.post;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;

import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.restdocs.AutoConfigureRestDocs;
import org.springframework.boot.test.autoconfigure.web.servlet.AutoConfigureMockMvc;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.context.annotation.Import;
import org.springframework.http.MediaType;
import org.springframework.restdocs.mockmvc.RestDocumentationRequestBuilders;
import org.springframework.test.annotation.Rollback;
import org.springframework.test.web.servlet.MockMvc;
import org.springframework.test.web.servlet.request.MockMvcRequestBuilders;
import org.springframework.transaction.annotation.Transactional;
import teamexpress.velo9.config.RestDocsConfig;

@AutoConfigureMockMvc
@AutoConfigureRestDocs
@SpringBootTest
@Import(RestDocsConfig.class)
class PostControllerTest {

	@Autowired
	private MockMvc mockMvc;

	@Test
	void writeGet() throws Exception {

		mockMvc.perform(MockMvcRequestBuilders.get("/write")
				.param("postId", "1"))
			.andExpect(status().isOk())
			.andDo(document("writeGet",
				requestParameters(
					parameterWithName("postId").description("새 글 작성 시 id 값 없이 url을 호출하면 됩니다."
						+ "글 수정 시 존재하는 postId를 담아 호출하시길 바랍니다.").optional()
				),
				relaxedResponseFields(
					fieldWithPath("id").description("작성중인 게시글의 id 입니다. post 방식으로 넘어갈 때 활용할 수 있습니다.").optional(),
					fieldWithPath("title").description("제목"),
					fieldWithPath("introduce").description("소개글"),
					fieldWithPath("content").description("본문"),
					fieldWithPath("access").description("공개설정"),
					fieldWithPath("memberId").description("작성글 주인의 식별 값입니다. post 방식으로 넘어갈 때 사용하면 좋을 것 같습니다."),
					fieldWithPath("seriesId").description("속해있는 시리즈의 id 입니다.").optional(),
					fieldWithPath("tagNames").description("달려있는 태그의 이름들 입니다.").optional(),
					fieldWithPath("postThumbnailDTO").description("썸네일 파일 이름 관련 정보가 들어 있을 수도 있습니다.").optional(),
					fieldWithPath("temporaryPostReadDTO").description("임시저장된 제목과 내용이 들어 있을 수도 있습니다.").optional()
				)
			));
	}

	//id반영
	@Test
	void series() throws Exception {
		this.mockMvc.perform(RestDocumentationRequestBuilders.get("/{nickname}/series", "admin"))
			.andExpect(status().isOk())
			.andDo(document("GetSeries", pathParameters(
					parameterWithName("nickname").description("유효한 회원의 닉네임을 입력해주세요")
				),
				relaxedResponseFields(
					fieldWithPath("content").description("시리즈네임과 최대 3개의 게시글들로 이루어져 있습니다.").optional(),
					fieldWithPath("size").description("한 페이지당 시리즈 개수입니다.").optional(),
					fieldWithPath("number").description("현재 페이지입니다.").optional(),
					fieldWithPath("first").description("첫 페이지인지 여부입니다.").optional(),
					fieldWithPath("last").description("끝 페이지인지 여부입니다.").optional(),
					fieldWithPath("numberOfElements").description("총 시리즈 개수입니다.").optional(),
					fieldWithPath("empty").description("내용이 없는지 여부입니다.").optional()
				)
			));
	}

	@Test
	@Transactional
	@Rollback
	void writePost() throws Exception {

		mockMvc.perform(post("/write")
				.content("{"
					+ "\n\"id\":1,"
					+ "\n\"title\":\"testtest\","
					+ "\n\"introduce\":\"1\","
					+ "\n\"content\":\"333333\","
					+ "\n\"access\":\"PRIVATE\","
					+ "\n\"memberId\":1,"
					+ "\n\"seriesId\":1,"
					+ "\n\"tagNames\":[\"A\",\"B\"],"
					+ "\n\"postThumbnailDTO\":{\"uuid\":\"1\", \"path\":\"bird\", \"name\":\"girl.avi\"}"
					+ "\n}")
				.contentType(MediaType.APPLICATION_JSON))
			.andExpect(status().isOk())
			.andDo(document("writePost",
				relaxedRequestFields(
					fieldWithPath("id").description("해당 필드가 없으면(null) 새 글(또는 임시글)이 작성됩니다.\n"
						+ "해당 필드가 있으면 기존에 존재하는 글(또는 임시글)의 수정(덮어쓰기)가 됩니다.").optional(),
					fieldWithPath("title").description(""),
					fieldWithPath("introduce").description("없으면 백엔드에서 알아서 content 일부 떼와서 넣어줍니다.").optional(),
					fieldWithPath("content").description(""),
					fieldWithPath("access").description("PUBLIC(전체공개), PRIVATE(비공개) 중에서 적습니다(물론, 없으면 PUBLIC입니다).").optional(),
					fieldWithPath("memberId").description("적절한 회원의 id를 주시길 바랍니다(세션 or get 방식의 memberId).\n"),
					fieldWithPath("seriesId").description("상세조건에서 시리즈를 선택하면 선택된 시리즈Object내부의 id값을 의미합니다.").optional(),
					fieldWithPath("tagNames").description("태그를 입력했다면, array로([]) 주십시오").optional(),
					fieldWithPath("postThumbnailDTO").description("필드 3개(uuid, path, name)를 가지고 있는 어떤 객체가 있을 수 있습니다.\n"
						+ "해당 객체를 변수로 지니고 있다가 주세요").optional()
				),
				responseFields(
					fieldWithPath("data").description("방금 작성(수정)된 글의 id 입니다. 작성(수정)후에는 작성된 게시글 상세보기로 가야합니다.")
				)
			));
	}

	//memberId없애고 다시
	//id?
	@Test
	void seriesPost() throws Exception {
		this.mockMvc.perform(get("/{nickname}/series/{seriesName}", "admin", "series1")
				.param("memberId", "2")
				.contentType(MediaType.APPLICATION_JSON))
			.andExpect(status().isOk())
			.andDo(document("GetSeriesPost",
				responseFields(
					fieldWithPath("content.[].id").description("포스트의 ID").optional(),
					fieldWithPath("content.[].title").description("제목").optional(),
					fieldWithPath("content.[].seriesName").description("시리즈 네임").optional(),
					fieldWithPath("content.[].introduce").description("소개글").optional(),
					fieldWithPath("content.[].createdDate").description("작성날짜").optional(),
					fieldWithPath("content.[].thumbnail.uuid").description("썸네일 uuid").optional(),
					fieldWithPath("content.[].thumbnail.name").description("썸네일 name").optional(),
					fieldWithPath("content.[].thumbnail.path").description("썸네일 path").optional(),
					fieldWithPath("content.[].postTags").description("포스트 태그들").optional(),
					fieldWithPath("pageable.sort.empty").description("password 내용").optional(),
					fieldWithPath("pageable.sort.unsorted").description("password 내용").optional(),
					fieldWithPath("pageable.sort.sorted").description("password 내용").optional(),
					fieldWithPath("pageable.offset").description("password 내용").optional(),
					fieldWithPath("pageable.pageNumber").description("password 내용").optional(),
					fieldWithPath("pageable.pageSize").description("password 내용").optional(),
					fieldWithPath("pageable.paged").description("password 내용").optional(),
					fieldWithPath("pageable.unpaged").description("password 내용").optional(),
					fieldWithPath("size").description("password 내용").optional(),
					fieldWithPath("sort.empty").description("password 내용").optional(),
					fieldWithPath("sort.unsorted").description("password 내용").optional(),
					fieldWithPath("sort.sorted").description("password 내용").optional(),
					fieldWithPath("number").description("password 내용").optional(),
					fieldWithPath("first").description("password 내용").optional(),
					fieldWithPath("last").description("password 내용").optional(),
					fieldWithPath("numberOfElements").description("password 내용").optional(),
					fieldWithPath("empty").description("password 내용").optional()
				)
			));
	}

	@Test
	@Transactional
	@Rollback
	void writeTemporary() throws Exception {

		mockMvc.perform(post("/writeTemporary")
				.content("{"
					+ "\n\"id\":2,"
					+ "\n\"title\":\"testTMP\","
					+ "\n\"content\":\"TMPTMP\","
					+ "\n\"memberId\":2"
					+ "\n}")
				.contentType(MediaType.APPLICATION_JSON))
			.andExpect(status().isOk())
			.andDo(document("writeTemporaryPost",
				requestFields(
					fieldWithPath("id").description("id가 없으면 새 글을 임시저장하는 경우(임시글목록에 보임)이며,"
						+ " id가 없으면 기존 임시글의 덮어쓰기(임시글목록에 보임) 또는"
						+ "기존 게시글의 대안 임시글을 생성(임시글목록에는 안보이고 수정화면에서 임시데이터가 있으면 불러오는 창나옴)"
						+ "하는 경우입니다.").optional(),
					fieldWithPath("title").description("title"),
					fieldWithPath("content").description("content"),
					fieldWithPath("memberId").description("/write와 마찬가지로 작성중인 회원의 id를 의미합니다.")
				)
			));
	}

	//id반영
	@Test
	void postsRead() throws Exception {
		this.mockMvc.perform(RestDocumentationRequestBuilders.get("/{nickname}/main", "admin")
				.param("page", "0"))
			.andExpect(status().isOk())
			.andDo(document("GetPostsRead",
				pathParameters(
					parameterWithName("nickname").description("유효한 닉네임")
				),
				requestParameters(
					parameterWithName("page").description("원하는 페이지 보다 1작은 값, 없으면 첫페이지").optional()
				),
				relaxedResponseFields(
					fieldWithPath("content").description("게시글 관련 정보들이 여러 개 있습니다.(제목, 댓글수, 썸네일, 태그 등)").optional(),
					fieldWithPath("size").description("-").optional(),
					fieldWithPath("number").description("-").optional(),
					fieldWithPath("first").description("-").optional(),
					fieldWithPath("last").description("-").optional(),
					fieldWithPath("numberOfElements").description("-").optional(),
					fieldWithPath("empty").description("-").optional()
				)
			));
	}

	@Test
	@Transactional
	@Rollback
	void delete() throws Exception {

		//임시로 love랑 look에 참조무결성 걸음

		mockMvc.perform(post("/delete")
				.param("postId", "1"))
			.andExpect(status().isOk())
			.andDo(document("delete",
				requestParameters(
					parameterWithName("postId").description("삭제할 게시글의 id 입니다.")
				)
			));
	}

	//id 반영
	@Test
	void tempPostsRead() throws Exception {
		this.mockMvc.perform(get("/temp")
				.param("memberId", "2"))
			.andExpect(status().isOk())
			.andDo(document("GetTemp",
				requestParameters(
					parameterWithName("memberId").description("로그인된 사용자의 id 값입니다.")
				),
				relaxedResponseFields(
					fieldWithPath("data").description("임시글 목록에 나올 게시글들의 요약정보들이 들어있습니다.").optional()
				)
			));
	}

	@Test
	void lovePostRead() throws Exception {
		this.mockMvc.perform(get("/archive/like")
				.param("memberId", "2"))
			.andExpect(status().isOk())
			.andDo(document("GetLovePostRead",
				requestParameters(
					parameterWithName("memberId").description("로그인된 사용자의 id 값입니다.")
				),
				relaxedResponseFields(
					fieldWithPath("content").description("좋아요를 누른 게시글의 요약 정보들이 들어있습니다.").optional(),
					fieldWithPath("number").description("-").optional(),
					fieldWithPath("first").description("-").optional(),
					fieldWithPath("last").description("-").optional(),
					fieldWithPath("numberOfElements").description("-").optional(),
					fieldWithPath("empty").description("-").optional()
				)
			));
	}

	@Test
	void lookPostRead() throws Exception {
		this.mockMvc.perform(get("/archive/recent")
				.param("memberId", "2"))
			.andExpect(status().isOk())
			.andDo(document("GetLookPostRead",
				requestParameters(
					parameterWithName("memberId").description("로그인된 사용자의 id 값입니다.")
				),
				relaxedResponseFields(
					fieldWithPath("content").description("최근 읽은 게시글의 요약 정보들이 들어있습니다.").optional(),
					fieldWithPath("number").description("-").optional(),
					fieldWithPath("first").description("-").optional(),
					fieldWithPath("last").description("-").optional(),
					fieldWithPath("numberOfElements").description("-").optional(),
					fieldWithPath("empty").description("-").optional()
				)
			));
	}

	@Test
	@Transactional
	@Rollback
	void love() throws Exception {
		mockMvc.perform(post("/love")
				.content("{"
					+ "\n\"postId\":1,"
					+ "\n\"memberId\":2"
					+ "\n}")
				.contentType(MediaType.APPLICATION_JSON))
			.andExpect(status().isOk())
			.andDo(document("love",
				requestFields(
					fieldWithPath("postId").description("좋아요 누른 게시글 id"),
					fieldWithPath("memberId").description("좋아요 누른 회원 id")
				)
			));
	}

	//memberId없애고 다시
	@Test
	void readPost() throws Exception {
		this.mockMvc.perform(get("/{nickname}/read/{postId}", "admin", "2")
				.param("memberId", "2")
				.contentType(MediaType.APPLICATION_JSON))
			.andExpect(status().isOk())
			.andDo(document("GetReadPost",
				responseFields(
					fieldWithPath("title").description("제목").optional(),
					fieldWithPath("seriesName").description("소개글").optional(),
					fieldWithPath("content").description("소개글").optional(),
					fieldWithPath("loveCount").description("소개글").optional(),
					fieldWithPath("createdDate").description("작성날짜").optional(),
					fieldWithPath("memberDTO.memberName").description("작성날짜").optional(),
					fieldWithPath("memberDTO.memberIntroduce").description("작성날짜").optional(),
					fieldWithPath("memberDTO.socialGithub").description("작성날짜").optional(),
					fieldWithPath("memberDTO.socialEmail").description("작성날짜").optional(),
					fieldWithPath("memberDTO.memberThumbnailDTO.uuid").description("작성날짜").optional(),
					fieldWithPath("memberDTO.memberThumbnailDTO.name").description("작성날짜").optional(),
					fieldWithPath("memberDTO.memberThumbnailDTO.path").description("작성날짜").optional(),
					fieldWithPath("postTags").description("작성날짜").optional(),
					fieldWithPath("prevPost").description("작성날짜").optional(),
					fieldWithPath("prevPostId").description("작성날짜").optional(),
					fieldWithPath("nextPost").description("작성날짜").optional(),
					fieldWithPath("nextPostId").description("작성날짜").optional()
				)
			));
	}

}
