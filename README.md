##### 语言： 中文 | [English](README_EN.md)

# Auto-Trim

A small and nice APT tool.
一个小巧且nice的APT工具。

Auto-Trim使用Java编译时注解处理器（ Annotation Processing Tool）帮助你自动生成String参数的String.trim()的操作。

## 注解

- `@AutoTrim` 对注释的对象进行AutoTrim操作，即添加 `string == null ? null : string.trim()` 代码.
- `@AutoTrim.Ignored` 进行AutoTrim操作时，忽略对该注释的对象。

## 特性

- 小巧，单一目标功能，简单易用;
- 支持JDK8~JDK21;
- 支持class类、接口、子类、匿名内部类的类级别；
- 支持属性上使用；
- 支持方法上使用；
- 支持方法形参上使用；
- 支持final修饰的形参；

## 注意

- **仅支持** `java.lang.String` 类型的变量进行AutoTrim操作;
- **不支持** `record` 类型的类构造器;

# 1. 使用准备

## 1.1 添加依赖

### Maven

```xml

<dependency>
    <groupId>com.supalle</groupId>
    <artifactId>auto-trim</artifactId>
    <version>1.0.0</version>
    <scope>provided</scope>
</dependency>
```

可以在 [maven.org](https://central.sonatype.com/artifact/com.supalle/auto-trim/0.8.1/versions) 查看最新可用的版本。

# 2. 使用样例

## 2.1 Method

- 编译前

```java
package org.example;

import com.supalle.autotrim.AutoTrim;

public class Example {
    @AutoTrim
    public boolean login(String username, String password, Integer type) {
        System.out.println(username);
        System.out.println(password);
        System.out.println(type);
        return true;
    }
}
```

- 编译后，对方法所有`String`类型对形参进行了AutoTrim操作：

```java
package org.example;

public class Example {
    public Example() {
    }

    public boolean login(String username, String password, Integer type) {
        System.out.println(username == null ? null : username.trim());
        System.out.println(password == null ? null : password.trim());
        System.out.println(type);
        return true;
    }
}
```

### 使用`@AutoTrim.Ignored`注解忽略AutoTrim操作。

- 编译前

```java
package org.example;

import com.supalle.autotrim.AutoTrim;

public class Example {
    @AutoTrim
    public boolean login(String username, @AutoTrim.Ignored String password, Integer type) {
        System.out.println(username);
        System.out.println(password);
        System.out.println(type);
        return true;
    }
}
```

- 编译后，对方法所有`String`类型对形参进行了AutoTrim操作，但忽略了`@AutoTrim.Ignored`修饰对形参：

```java
package org.example;

public class Example {
    public Example() {
    }

    public boolean login(String username, String password, Integer type) {
        System.out.println(username == null ? null : username.trim());
        System.out.println(password);
        System.out.println(type);
        return true;
    }
}
```

## 2.2 Field

- 编译前，可以配合Lombok一起使用

```java
package org.example;

import com.supalle.autotrim.AutoTrim;
import lombok.Setter;

@Setter
public class Example {
    @AutoTrim
    private String username;
    private String password;
    private Integer type;
}

```

- 编译后，对`@AutoTrim`修饰对`String`类型属性的Setter方法进行了AutoTrim操作：

```java
package org.example;

public class Example {
    private String username;
    private String password;
    private Integer type;

    public Example() {
    }

    public void setUsername(String username) {
        this.username = username == null ? null : username.trim();
    }

    public void setPassword(String password) {
        this.password = password;
    }

    public void setType(Integer type) {
        this.type = type;
    }
}
```

## 2.3 Class

- 编译前，可以配合Lombok一起使用

```java
package org.example;

import com.supalle.autotrim.AutoTrim;
import lombok.Setter;

@Setter
@AutoTrim
public class Example {
    private String username;
    private String password;
    private Integer type;
}

```

- 编译后，对类所有的有`String`类型形参的方法进行了AutoTrim操作，注意，包括非Setter的方法：

```java
package org.example;

public class Example {
    private String username;
    private String password;
    private Integer type;

    public Example() {
    }

    public void setUsername(String username) {
        this.username = username == null ? null : username.trim();
    }

    public void setPassword(String password) {
        this.password = password == null ? null : password.trim();
    }

    public void setType(Integer type) {
        this.type = type;
    }
}
```

## 2.4 Interface

- 编译前

```java
package com.supalle;

import com.supalle.autotrim.AutoTrim;

import java.util.Objects;

@AutoTrim
public interface UserService {
    default boolean login(String username, @AutoTrim.Ignored String password, Integer type) {
        System.out.println(username);
        System.out.println(password);
        return Objects.equals(username, password);
    }
}
```

- 编译后，对类所有的有`String`类型形参的方法进行了AutoTrim操作，`@AutoTrim.Ignored`同样好使：

```java
package com.supalle;

import java.util.Objects;

public interface UserService {
    default boolean login(String username, String password, Integer type) {
        username = username == null ? null : username.trim();
        System.out.println(username);
        System.out.println(password);
        return Objects.equals(username, password);
    }
}
```

## 3. 补充

### 3.1 注解优先级

- 原则1：越接近形参的注解，优先级越高；
- 原则2：同一注解对象上，`@AutoTrim.Ignored`优先级比`@AutoTrim`高；

顺序：形参`@AutoTrim.Ignored` > 形参`@AutoTrim` > 方法`@AutoTrim.Ignored` > 方法`@AutoTrim` > 属性`@AutoTrim.Ignored` >
属性`@AutoTrim` > 类`@AutoTrim.Ignored` > 类`@AutoTrim`

### 3.2 `@AutoTrim`的本质

***`@AutoTrim`的本质是给方法所有的`String`形参，在使用前合理的添加一段`string == null ? null : string.trim()` 代码。***

## 4. 鸣谢

- 感谢 [Lombok](https://github.com/projectlombok/lombok) 的代码开源，给予我参考。
