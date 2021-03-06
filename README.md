# x2ai
**SLA**
* training model: given the well-defined examples, automatically preprocessing, training and tuning.
* prediction: recommend the ranked items which the users are most likely interested with based on the fine trained models.

**Architecture**  
 * front end
     * _html/css/js/jQuery_
 * back end
     * _Jersey_: RESTful web service.
 * recommendation engine
     * _XMan_: powered by scikit-learn.
     * _XETLer_: data ETL tool implemented with java.

**Deployment**
- JDK 1.8
- Tomcat 8
- Anaconda 3

**blueprint with future work**  
![blueprint](x2ai/src/main/webapp/pic/design.jpg?raw=true "Title")
