# SpringBoot-RegAuth

SpringBoot registration and authentication.

### Генерация ключей для подписки
> $ openssl ecparam -genkey -name prime256v1 -out private_key.pem
> 
> $ openssl ec -in private_key.pem -pubout -outform DER|tail -c 65|base64|tr -d '=' |tr '/+' '_-' >> public_key.txt
> 
> $ openssl ec -in private_key.pem -outform DER|tail -c +8|head -c 32|base64|tr -d '=' |tr '/+' '_-' >> private_key.txt

### По умолчанию создаются пользователи:
>
> admin с паролем 111111
> 
> user с паролем 111111

### В проекте используются:
>
> Bootstrap v5.0.2 (https://getbootstrap.com/)
>
> Copyright 2011-2021 The Bootstrap Authors
>
> Copyright 2011-2021 Twitter, Inc.
>
> Licensed under MIT (https://github.com/twbs/bootstrap/blob/main/LICENSE)

> Bootstrap Utilities v5.0.2 (https://getbootstrap.com/)
> 
> Copyright 2011-2021 The Bootstrap Authors
> 
> Copyright 2011-2021 Twitter, Inc.
> 
> Licensed under MIT (https://github.com/twbs/bootstrap/blob/main/LICENSE)

> Bootstrap Icons v1.9.0 (https://icons.getbootstrap.com/)
>
> Official open source SVG icon library for Bootstrap.
>
> Licensed under MIT (https://github.com/twbs/icons/blob/main/LICENSE.md)

