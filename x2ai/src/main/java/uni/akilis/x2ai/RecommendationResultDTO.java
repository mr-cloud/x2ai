package uni.akilis.x2ai;

import java.io.Serializable;

public class RecommendationResultDTO implements Serializable{

	/**
	 * 
	 */
	private static final long serialVersionUID = 1L;
	
	private String telephoneNumber;
	private Double score;
	public RecommendationResultDTO(String _tele, Double _score) {
		// TODO Auto-generated constructor stub
		this();
		this.telephoneNumber = _tele;
		this.score = _score;
	}
	public RecommendationResultDTO() {
		// TODO Auto-generated constructor stub
	}
	public String getTelephoneNumber() {
		return telephoneNumber;
	}
	public void setTelephoneNumber(String telephoneNumber) {
		this.telephoneNumber = telephoneNumber;
	}
	public Double getScore() {
		return score;
	}
	public void setScore(Double score) {
		this.score = score;
	}
	
	
	
}
