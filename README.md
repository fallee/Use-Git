Use-Git
=======
init
-------
### * authentication 

```
$ ssh-keygen
$ cat .ssh/id_rsa.pub
```

> copy id_rsa.pub to github.com user setting's ssh key list

### * set user name

```
$ git config user.name "fallseir at 1.59"
$ git config user.email "fallseir@gmail.com"
```

### * init repo
```
$ git clone https://github.com/fallseir/Use-Git.git Fs.Use-Git
```

work
------
### * commit
> edit README.md

```
$ git status 
$ git add README.md
$ git commit -m "append markdown syntax introduce"
```

### * check

```
$ git log
$ git config alias.logc "log --color --graph --pretty=format:'%Cred%h%Creset \
-%C(yellow)%d%Creset %s %Cgreen(%cd) %C(bold blue)<%an>%Creset' --date=iso" 
$ git logc
$ git diff HEAD -- README.md
```

### * remote

```
$ git remote add up git@github.com:fallseir/Use-Git.git
$ git push up master:master
$ git fetch up
```

### * branch

```
$ git branch -a
$ git checkout -b editREADME
$ git push up editREADME:master
$ git push up editREADME
```



HELP Markdown syntax for .md file
====

``` 
# H1
## H2
### H3
#### H6

Title
===
sub title
---

`` code ``

> blockquote

link
<auto_link_url>

[link name](url "title")
[link name][refid]
[refid][]

[refid]: url "title"

image
![alt text](path/to/img.jpg "optional title")


*single asterisks*
_single underscores_




- list
+ list too
* same list
1. list
3. list

```

