# Fascicolo Sanitario 2.0

# _it-fse-gtw-status-manager_

Il microservizio in oggetto ha la responsabilità di accogliere e salvare i log di stato provenienti dai microservizi del Gateway al fine di permettere ad it-fse-gtw-status-check l'esposizione verso il Consumer.

<br/>

## Test ISOLATION

Con Java 21 e Maven, eseguire `MAVEN_OPTS='-Xmx768m' mvn -PISOLATION clean test`.
Il profilo seleziona esclusivamente i test JUnit con tag `ISOLATION`; questi sono
esclusi dall'esecuzione Maven predefinita. `@ActiveProfiles("ISOLATION")` configura
Spring e non sostituisce la selezione tramite tag.

I test in `src/test/java/it/finanze/sanita/fse2/ms/gtw/statusmanager/isolation`
usano contesti Spring minimi (mai l'applicazione completa), con dipendenze esterne
mock: nessun MongoDB, broker Kafka, servizio HTTP o container richiesto. Coprono
repository e policy privacy, notifiche di stato finale, callback, consumo Kafka,
cache configurazione, client/route HTTP e scheduler con retry e percorsi di errore.
I retry dello scheduler mantengono il backoff reale e richiedono alcuni secondi.

Report JaCoCo: `target/site/jacoco/index.html` e `target/site/jacoco/jacoco.xml`.
I test Kafka verificano configurazioni producer/consumer, opzioni di sicurezza e
pubblicazione dead-letter con template mock, senza avviare connessioni al broker.

## Repos
- [*it-fse-support*](https://github.com/ministero-salute/it-fse-support) - Pagina di supporto FSE 2.0
- [*it-fse-landing*](https://github.com/ministero-salute/it-fse-landing) - Landing page dei repository FSE 2.0

<br/>

## License

[![License: AGPL v3](https://img.shields.io/badge/License-AGPL_v3-blue.svg)](https://www.gnu.org/licenses/agpl-3.0)
