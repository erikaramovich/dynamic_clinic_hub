```text
com.miro.project
├── config
│   ├── Flyway.java  
│   ├── SecurityConfig.java
│   └── SwaggerConfig.java  
│   
└── controller
│   └── AuthController.java
│   
├── dto
│   ├── request
│   │   ├── LoginRequest.java
│   │   └── RegisterRequest.java
│   │
│   └── response
│       ├── AuthResponse.java  
│       └── ErrorResponse.java   
│   
├── exception
│   ├── GlobalExceptionHandler.java  
│   ├── InvalidCredentialsException.java
│   ├── TokenRefreshException.java
│   └── UserAlreadyExistsException.java 
│ 
├── model
│   ├── Role.java              
│   ├── User.java               
│   └── RefreshToken.java
│ 
├── repository
│   ├── RefreshTokenRepository.java
│   └── UserRepository.java
│ 
├── security
│   ├── JwtProvider.java         
│   ├── UserDetailsImpl.java     
│   └── UserDetailsServiceImpl.java 
│ 
├── service
│   └── AuthService.java 
│        
└──AuthenticationMsApplication.java
```