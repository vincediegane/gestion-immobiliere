package sn.gestionimmobiliere.backend.identity.api;public record LoginResponse(String accessToken,String tokenType,long expiresInSeconds,String fullName,String role){}
