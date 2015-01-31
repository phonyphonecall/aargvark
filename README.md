# aargvark
Concise annotation driven argument parsing for java.

## Examples
The goal is to give the program one source of truth for all arguments, with almost no work for the user.

In our example here we need to configure a client with a server host, and port. The first step is to create the class to hold configuration information.

```java
    @Aargvark
    public static class Config {

        @Aargument(shortName = 'h', usage = "the hostname or IP address of the server", require = true)
        public String host;

        @Aargument(shortName = 'p', usage = "the port number", require = true)
        public int port;
    }
```

Now to parse those argmuments
```java
public static void main(String[] args) {
  Config config = new Config();
  try {
    new KingAargvark(args).marshal(config);
  }  catch (AargvarkException e) {
    e.printStackTrace();
  }
  connectToServer(config.host, config.port);
  ...
}
```
Finally lets run our client.
```sh
java client -h host.com -p 8080
```
or
```sh
java client --host host.com --port 8080
```

By letting aargvark do the heavy lifting, users get flexable and simple argument parsing, with almost no boilerplate.

##Features

###Usage Printing
```
> java client --help
Usage:
  -h  --host  the hostname or IP address of the server
  -p  --port  the port number
```


