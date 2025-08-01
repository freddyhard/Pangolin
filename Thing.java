import java.io.Serializable;

public class Thing implements Serializable
{
	
	/**
	 * This just holds either a question or an answer. THen links to next question with a y or n answer and a link to the previous question
	 */
	private static final long serialVersionUID = 1L;
	private Long linkToParent;
	private Long linkToYes;
	private Long linkToNo;
	private String question;
	private String answer;
	
	
	public Thing(Long linkToParent, Long linkToYes, Long linkToNo, String question, String answer)
	{
		this.linkToParent = linkToParent;
		this.linkToYes = linkToYes;
		this.linkToNo = linkToNo;
		this.question = question;
		this.answer = answer;
	}
	
	public Long getLinkToParent()
	{
		return linkToParent;
	}


	public void setLinkToParent(Long linkToParent)
	{
		this.linkToParent = linkToParent;
	}


	public Long getLinkToYes()
	{
		return linkToYes;
	}


	public void setLinkToYes(Long linkToYes)
	{
		this.linkToYes = linkToYes;
	}


	public Long getLinkToNo()
	{
		return linkToNo;
	}


	public void setLinkToNo(Long linkToNo)
	{
		this.linkToNo = linkToNo;
	}


	public String getQuestion()
	{
		return question;
	}


	public void setQuestion(String question)
	{
		this.question = question;
	}


	public String getAnswer()
	{
		return answer;
	}


	public void setAnswer(String answer)
	{
		this.answer = answer;
	}




}
