# Verified Prototype Regulatory Dataset

The prototype contains a deliberately small, source-backed Maharashtra dataset. It is not a claim of complete Maharashtra coverage.

## MPCB Consent Management
Source: https://www.mpcb.gov.in/en/consentmgt/water-and-air-act

Prototype facts: MPCB describes Consent to Establish as required prior to establishing an industry or process when consent is applicable, and Consent to Operate after establishment with required pollution-control systems.

## MPCB Application Information
Source: https://mpcb.gov.in/en/node/4201

Prototype facts: the official page lists CA Certificate, Balance Sheet, Capital Investment, Manufacturing Process, Industry Registration, Land Ownership Certificate and a detailed pollution-control-system proposal for Consent to Establish. It also lists Previous Consent Copy for Consent to Operate/Renewal.

## MPCB notified service timelines
Source: https://www.mpcb.gov.in/en/node/6866

Prototype fact: the notified-services table includes category and capital-investment based time limits; the Green-category Consent to Establish row includes 30 days for capital investment up to Rs. 100 crore. The prototype stores this as a 30-day target only when the business explicitly carries the GREEN pollution category and capital investment does not exceed Rs. 100 crore.

## Demo boundary
The demo profile explicitly declares `environmentalConsentRequired=true`. The rule engine does not infer environmental consent merely from a sector name.

## FSSAI food business licensing / registration
Source: https://fssai.gov.in/business/licensing

Prototype fact: FSSAI states that every Food Business Operator is required to be licensed/registered under the FSS Act. The second reference business explicitly declares `foodBusinessOperator=true`; the rule engine does not infer that status from a sector label.
