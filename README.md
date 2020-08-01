# IOh

![Maven Central](https://img.shields.io/maven-central/v/io.github.kag0/ioh_2.13?style=for-the-badge)

`() => Future(println("oh"))`

Add referential transparency and laziness with `Future`.
Making it safe(r) to use `Future` with tagless-final.

Say we want to adopt some cats, we just need to name them.
```scala
def adoptCats[F[_]: Monad](generateName: F[String]): F[String] = 
  for {
    cat1 <- generateName
    cat2 <- generateName
  } yield s"enjoy your new cats, $cat1 and $cat2"
```

Using `Future` it's going to be confusing having two cats with the same name.
```scala
adoptCats(Future(pickACatNameAtRandom()))
  .onSuccess(println) 
// enjoy your new cats, fluffy and fluffy
```  

`IOh` is just an alias for `() => Future`, 
so just add the lambda to our previous code and we're good to go.
```scala
import io.github.kag0.ioh._
adoptCats(() => Future(pickACatNameAtRandom()))
  .apply()
  .onSuccess(println) 
// enjoy your new cats, fluffy and snowball
```

Adds instances for `Bracket`, `Async`, and `Parallel` which provides
* `Functor`
* `Applicative`
* `Monad`
* `MonadError`
* `Defer`
* `Sync`

### Why is this useful?

Most errors around the lack of referential transparency in `Future` are not 
coming from developers using `Future` directly in their code. 
`Future`'s semantics are generally understood, and code is structured around it.
The issues arise when tagless-final comes into the picture, 
and the developers working with the tagless-final code are assuming their `F`is 
safe.
Many libraries in the Scala ecosystem use tagless-final so that their users can 
decide which side effect or async monad they want to use (this is a good thing).

`IOh` exists for the codebases which are using `Future` and interfacing with 
tagless-final code, to protect them from violating the assumptions made by the 
developers working on the tagless-final code.  
Lifting a `Future` to an `IOh` is as simple as wrapping that `Future`'s creation 
in a `Function0`.
