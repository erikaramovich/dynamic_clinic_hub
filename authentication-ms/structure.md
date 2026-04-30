```text
com.miro.project
├── model
│   ├── Role.java              
│   ├── User.java               
│   └── RefreshToken.java
├── dto
│   ├── request
│   │   ├── RegisterRequest.java
│   │   └── LoginRequest.java
│   └── response
│       └── AuthResponse.java    
├── repository
│   ├── UserRepository.java
│   └── RefreshTokenRepository.java
├── security
│   ├── JwtProvider.java         
│   ├── UserDetailsImpl.java     
│   └── UserDetailsServiceImpl.java 
├── config
│   └── SecurityConfig.java      
├── service
│   └── AuthService.java         
└── controller
└── AuthController.java
```