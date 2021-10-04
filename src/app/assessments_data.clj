(ns app.assessments-data
  (:require [clojure.string :as str]
            [inflections.core :as inf]
            [honey.sql  :as sql]
            [honey.sql.helpers :refer :all :as h]
            [app.server :refer [db-spec]]
            [next.jdbc :as jdbc]))


(def domains-text ["Plan&Organize Define a strategic IT plan that satisfies the business requirement for IT of sustaining or  extending the business strategy and governance requirements whilst being transparent about benefits, costs and risks  "  "Acquire&Implement Identify automated solutions that satisfies the business requirement for IT of translating  business functional and control requirements into an effective and efficient design of automated solutions "  "Distribute&Support-1 Define and manage service levels that satisfies the business requirement for IT of ensuring  the alignment of key IT services with the business strategy  "  "Distribute&Support-2...part 2"  "Monitor&Evaluate Monitor and evaluate IT performance that satisfies the business requirement for IT of  transparency and understanding of IT cost, benefits, strategy, policies and service levels in accordance with governance  requirements  "])

(def domains
  (->> (mapv #(str/split % #" |\.\.\.") domains-text)
       (map-indexed (fn [idx itm] (hash-map :domain-id (inc idx) :name (first itm) :description (str/join " " (rest itm)))))))


(def topics-text ["StrategicITPlan  InformationArchitecture  TechnologicalDirection  ITOrganisation  ITInvestment  CommunicateDirection  ITHumanResources  QualityMgmt  ITRiskMgmt  ProjectsMgmt"
                  "AutomatedSolutions  ApplicationSoftware  TechnologyInfrastructure  OperationsUse  ITResources  ChangeMgmt  ChangesInstallation"
                  "ServiceLevels  Services3rdParties  PerformanceCapacity  Continuity  SecuritySystems  CostAllocation  UserEducation"
                  "IncidentMgmt  ConfigurationMgmt  ProblemMgmt  DataMgmt  PhysicalEnvironment  ITOperationsMgmt"
                  "ITPerformanceMonitoring  InternalControl  ExternalRequirements  ITGovernance"])

(def topic-domains {:1 0
                    :2 10
                    :3 17
                    :4 24
                    :5 30})

(def topics
  (->> (map-indexed (fn [i v]
                      (->> (str/split v #"  ")
                           (map-indexed (fn [idx itm]
                                          (hash-map :description ""
                                                    :topic-id (+ ((keyword (str (inc i))) topic-domains)  (inc idx))
                                                    :domain-id (inc i)
                                                    :name  itm)))))
                    topics-text)
       flatten))

(def questions-text ["MUL  1  Management of the process of Define a strategic IT plan that satisfies the business requirement for IT of sustaining or extending the business strategy and governance requirements whilst being transparent about benefits, costs and risks  IT strategic planning is not performed.  There is no management awareness that IT strategic planning is needed to support business goals."
                     "MUL  1  Management of the process of Define a strategic IT plan that satisfies the business requirement for IT of sustaining or extending the business strategy and governance requirements whilst being transparent about benefits, costs and risks  The need for IT strategic planning is known by IT management.  IT planning is performed on an as-needed basis in response to a specific business requirement.  IT strategic planning is occasionally discussed at IT management meetings.  The alignment of business requirements, applications and technology takes place reactively rather than by an organisationwide strategy.  The strategic risk position is identified informally on a project-by-project basis."
                     "MUL  2  Management of the process of Define the information architecture that satisfies the business requirement for IT of being agile in responding to requirements, to provide reliable and consistent information, and to seamlessly integrate applications into business processes.  There is no awareness of the importance of the information architecture for the organisation.  The knowledge, expertise and responsibilities necessary to develop this architecture do not exist in the organisation."
                     "MUL  2  Management of the process of Define the information architecture that satisfies the business requirement for IT of being agile in responding to requirements, to provide reliable and consistent information, and to seamlessly integrate applications into business processes.  Management recognises the need for an information architecture.  Development of some components of an information architecture is occurring on an ad hoc basis.  The definitions address data, rather than information, and are driven by application software vendor offerings.  There is inconsistent and sporadic communication of the need for an information architecture."
                     "MUL  2  Management of the process of Define the information architecture that satisfies the business requirement for IT of being agile in responding to requirements, to provide reliable and consistent information, and to seamlessly integrate applications into business processes.  An information architecture process emerges and similar, though informal and intuitive, procedures are followed by different individuals within the organisation.  Staff obtain their skills in building the information architecture through hands-on experience and repeated application of techniques.  Tactical requirements drive the development of information architecture components by individual staff members."
                     "MUL  3  Management of the process of Determine technological direction that satisfies the business requirement for IT of having stable, cost-effective, integrated and standard application systems, resources and capabilities that meet current and future business requirements.  There is no awareness of the importance of technology infrastructure planning for the entity.  The knowledge and expertise necessary to develop such a technology infrastructure plan do not exist.  There is a lack of understanding that planning for technological change is critical to effectively allocate resources."
                     "MUL  4  Management of the process of Determine technological direction that satisfies the business requirement for IT of having stable, cost-effective, integrated and standard application systems, resources and capabilities that meet current and future business requirements.  Management ensures the development and maintenance of the technology infrastructure plan.  IT staff members have the expertise and skills necessary to develop a technology infrastructure plan.  The potential impact of changing and emerging technologies is taken into account.  Management can identify deviations from the plan and anticipate problems.  Responsibility for the development and maintenance of a technology infrastructure plan has been assigned.  The process of developing the technology infrastructure plan is sophisticated and responsive to change.  Internal good practices have been introduced into the process.  The human resources strategy is aligned with the technology direction, to ensure that IT staff members can manage technology changes.  Migration plans for introducing new technologies are defined.  Outsourcing and partnering are being leveraged to access necessary expertise and skills.  Management has analysed the acceptance of risk regarding the lead or lag use of technology in developing new business opportunities or operational efficiencies."
                     "SEL  11  Management of the process of Identify automated solutions that satisfies the business requirement for IT of translating business functional and control requirements into an effective and efficient design of automated solutions  Clear and structured approaches in determining IT solutions exist.  The approach to the determination of IT solutions requires the consideration of alternatives evaluated against business or user requirements, technological opportunities, economic feasibility, risk assessments, and other factors.  The process for determining IT solutions is applied for some projects based on factors such as the decisions made by the individual staff members involved, the amount of management time committed, and the size and priority of the original business requirement.  Structured approaches are used to define requirements and identify IT solutions."])


(def questions
  (->> (map-indexed
         (fn [i v]
           (->> (str/split v #"  ")
                (#(hash-map :topic-id (Integer. (second %))
                            :question-id  (inc i)
                            :question-type [:cast (first %) :question-type]
                            :text (nth % 2)))))
                            ;:answers (->> (drop 4 %)
                            ;              (map-indexed (fn [idx q] (hash-map :id (keyword (str (second %) "."  (nth % 2) "." i "." (str idx)))
                            ;                                                 :text q)))
                            ;              (map (fn [x] (apply merge (map (fn [a] (hash-map (keyword  "answer"  (name (first a))) (second a))) x)))))))))
         questions-text)
       flatten))

(defn make-sql [entity]
 (-> (insert-into (keyword (inf/singular entity)))
  (values @(resolve (symbol entity)))
  (sql/format)))


(defn exec-sql [entity]
  (with-open [con (jdbc/get-connection db-spec)]
   (jdbc/execute! con (make-sql entity))))


;(defn modify-sql [entity]
;  (let [sql (make-sql entity)
;        sql- (str (first (str/split (first sql) #"VALUES \(" 2)) " VALUES")
;        ;_ (tap> (rest sql))
;        values (partition 4 (rest sql))]
;    [sql- values]))

(comment
  INSERT INTO domain (description, name, domain_id) VALUES
  ('Define a strategic IT plan that satisfies the business requirement for IT of sustaining or  extending the business strategy and governance requirements whilst being transparent about benefits, costs and risks',
    'Plan&Organize',
    1),
  ('Identify automated solutions that satisfies the business requirement for IT of translating  business functional and control requirements into an effective and efficient design of automated solutions',
    'Acquire&Implement',
    2),
  ('Define and manage service levels that satisfies the business requirement for IT of ensuring  the alignment of key IT services with the business strategy',
    'Distribute&Support-1',
    3),
  ('part 2',
      'Distribute&Support-2',
    4),
  ('Monitor and evaluate IT performance that satisfies the business requirement for IT of  transparency and understanding of IT cost, benefits, strategy, policies and service levels in accordance with governance  requirements',
    'Monitor&Evaluate',
    5));)
