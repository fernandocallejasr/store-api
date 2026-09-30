package io.github.fernandocallejasr.stickerstore.dto;

import io.github.fernandocallejasr.stickerstore.util.Emails;
import jakarta.validation.constraints.Email;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.Pattern;
import jakarta.validation.constraints.Size;

import java.text.Normalizer;
import java.util.Objects;

public class RegisterRequest {
	@NotBlank(message = "Name is required")
	@Size(min = 2, max = 30, message = "The length of the name should be between {min} and {max} characters")
	@Pattern(regexp = "^\\p{L}[\\p{L}\\p{M} '\\u2019.-]*$", message = "Name can only contain letters, spaces, apostrophes, hyphens and periods")
	private String name;

	@NotBlank(message = "Email is required")
	@Email(message = "Email address must be a valid value")
	@Size(max = 100, message = "Email must be at most {max} characters")
	private String email;

	@NotBlank(message = "Mobile number is required")
	@Pattern(regexp = "^\\d{10}", message = "Mobile number must be exactly 10 digits")
	private String mobileNumber;

	@NotBlank(message = "Password is required")
	@Size(min = 8, max = 20, message = "Password length must be between {min} and {max} characters")
	private String password;

	public RegisterRequest() {
	}

	public String getName() {
		return name;
	}

	public void setName(String name) {
		this.name = name == null ? null : Normalizer.normalize(name.strip(), Normalizer.Form.NFC);
	}

	public String getEmail() {
		return email;
	}

	public void setEmail(String email) {
		this.email = Emails.normalize(email);
	}

	public String getMobileNumber() {
		return mobileNumber;
	}

	public void setMobileNumber(String mobileNumber) {
		this.mobileNumber = mobileNumber == null ? null : mobileNumber.strip();
	}

	public String getPassword() {
		return password;
	}

	public void setPassword(String password) {
		this.password = password;
	}

	@Override
	public boolean equals(Object o) {
		if (o == null || getClass() != o.getClass())
			return false;
		RegisterRequest that = (RegisterRequest) o;
		return Objects.equals(name, that.name) &&
				Objects.equals(email, that.email) &&
				Objects.equals(mobileNumber, that.mobileNumber) &&
				Objects.equals(password, that.password);
	}

	@Override
	public int hashCode() {
		return Objects.hash(name, email, mobileNumber, password);
	}

	@Override
	public String toString() {
		return "RegisterRequest{" +
				"name='" + name + '\'' +
				", email='" + email + '\'' +
				", mobileNumber='" + mobileNumber + '\'' +
				", password='***'" +
				'}';
	}
}
