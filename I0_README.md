# I0

`I0` provides methods and integrations around standard library constructs to support all the following features, in one.

### Asynchronous computation

```scala
Future[A]
```

### Suspended side effects

```scala
() => A
```

### "Reader" dependency resolution

```scala
D => A
```

### Typed errors

```scala
Either[E, A]
```

## Combined

all together, we get

```scala
D => Future[Either[E, A]]
```

conveniently aliased as

```scala
I0[D, E, A]
```

along with all the methods you'd expect in order to use `I0` like any other standard library container

```scala
for {
  x <- myI0
  y  = doSomething(x)
  z <- getZ(y)
} yield z
```

