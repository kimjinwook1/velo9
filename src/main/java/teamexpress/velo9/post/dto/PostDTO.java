package teamexpress.velo9.post.dto;

import lombok.Data;

@Data
public class PostDTO {
	private static final int MAX = 150;
	private static final int FIRST_INDEX = 0;

	private Long id;
	private String title;
	private String introduce;
	private String content;
	//private Long thumbnailId;
	private String status;

	public boolean isIntroduceNull() {
		return this.introduce == null;
	}

	public void createIntroduce() {
		if (smallerThanMax(this.content)) {
			this.introduce = this.content;
			return;
		}
		this.introduce = this.content.substring(FIRST_INDEX, MAX);
	}

	private boolean smallerThanMax(String content) {
		return content.length() < MAX;
	}
}
