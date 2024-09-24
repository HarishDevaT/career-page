package com.harish.DataBase.Service;

import java.security.Key;
import java.security.NoSuchAlgorithmException;
import java.util.Base64;
import java.util.Date;
import java.util.HashMap;
import java.util.Map;
import java.util.function.Function;

import javax.crypto.KeyGenerator;
import javax.crypto.SecretKey;

import org.springframework.security.core.userdetails.UserDetails;
import org.springframework.stereotype.Service;

import io.jsonwebtoken.Claims;
import io.jsonwebtoken.Jwts;
import io.jsonwebtoken.io.Decoders;
import io.jsonwebtoken.security.Keys;

@Service
public class JWTService {

	private String secretKey = "" ;
	private String userName = "" ;
	
	public JWTService() {
		KeyGenerator keyGen;
		try {
			keyGen = KeyGenerator.getInstance("HmacSHA256");
			SecretKey sk = keyGen.generateKey() ;
			secretKey = Base64.getEncoder().encodeToString(sk.getEncoded());
		} 
		catch (NoSuchAlgorithmException e) {
			throw new RuntimeException(e) ;
		}
	}

	public String generateToken(String userName) {
		
		Map<String,Object> claims = new HashMap<>();
		
		
		return Jwts
				.builder()
				.claims()
				.add(claims)
				.subject(userName)
				.issuedAt(new Date(System.currentTimeMillis()))
				.expiration(new Date(System.currentTimeMillis()+60*60*60))
				.and()
				.signWith(getKey())
				.compact() ;
	}

	private SecretKey getKey() {
		byte[] keyBytes = Decoders.BASE64.decode(secretKey);
		return Keys.hmacShaKeyFor(keyBytes);
	}

	public String extractUserName(String token) {
		return extractClaim(token,Claims::getSubject);
	}

	private <T> T extractClaim(String token, Function<Claims,T> claimResolver) {
		final Claims claims = extractAllClaims(token);
		return claimResolver.apply(claims);
	}

	private Claims extractAllClaims(String token) {
		return Jwts
				.parser()
				.verifyWith(getKey())
				.build()
				.parseSignedClaims(token)
				.getPayload() ;
	}
	
	public String getUserName() {
		return userName ;
	}

	public boolean validateToken(String token, UserDetails userDetails) {
		final String finalUserName = extractUserName(token);
		userName = finalUserName ;
		return (finalUserName.equals(userDetails.getUsername())&& !isTokenExpired(token));
	}

	private boolean isTokenExpired(String token) {
		return extractExpiration(token).before(new Date());
	}

	private Date extractExpiration(String token) {
		return extractClaim(token,Claims::getExpiration);
	}

}
