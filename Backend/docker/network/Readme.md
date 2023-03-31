On utilise https://github.com/lukaszlach/docker-tc pour limiter la bande passante du réseau.

ATTENTION : docker-tc ne marche que sur un environement Linux classique de type Ubuntu ou Fedora. WSL et MacOS ne sont pas supportés.

- Aller dans `docker-tc` et faire `docker-compose up` pour lancer le limiteur de bande passante
- Aller dans `./` et faire `docker-compose up` pour lancer les DrimBox   