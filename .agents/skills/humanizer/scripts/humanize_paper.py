#!/usr/bin/env python3
"""
Humanizer script for skill-lens.docx.
Applies humanized text to document paragraphs while preserving document styles and structure.
Runs detection analysis to verify 0% AI detection.
"""

import sys
import docx
from pathlib import Path
from detect_ai_patterns import analyze_text

# Mapping of paragraph index to humanized text
HUMANIZED_PARAGRAPHS = {
    0: "SkillLens: Evidence-Based Skill Verification and Career Intelligence Using Resume and GitHub Evidence",
    1: "A Java Desktop Prototype for Validating Student Technical Skills and Career Progression",
    2: "Authors: Deepak Rakshit, Shaswat Srivastav, Arya Kashyap    |    Institution: Noida Institute of Engineering and Technology",
    3: "Abstract",
    4: "Technical hiring and internship screening rely heavily on unverified resume claims. Candidates list technical tools and frameworks without indicating their depth of practical experience. SkillLens pairs resume parsing with public GitHub activity and explicit role definitions to verify practical code exposure. The desktop application parses submitted resumes in PDF or DOCX format, extracts technical skills into canonical forms, and inspects public repositories alongside commit records from default branches. Claims map onto five categories: verified, partially verified, weak evidence, unverified, or missing. A deterministic formula calculates numerical role-fit scores, while large language models handle explanatory tasks, including career advice and profile optimization. The system uses Google Gemini as its primary model and routes to Groq's GPT-OSS 120B when Gemini is unavailable. Mentorship runs through a local retrieval-augmented generation pipeline isolated by candidate identifier. Built with Java 25, JavaFX 25, Spring Boot, and Maven, the tool runs locally without external web servers or Python environments. In a test run using an applicant resume and 111 public repositories, the prototype collected 97 commits, formed 43 evidence links, and indexed 57 local text chunks. While public code cannot capture private workplace contributions, it offers an inspectable baseline for assessing entry-level technical readiness.",
    5: "Keywords: skill verification, GitHub mining, resume analysis, semantic matching, explainable AI, retrieval-augmented generation, career roadmap, Java, generative AI, evidence-based recruitment.",
    6: "1. Introduction",
    7: "Job applications depend on self-reported resumes. Candidates often list tools and programming languages without clarifying whether they built production systems or followed a tutorial. Standard screening tools compute text similarity between resumes and job postings. Similarity metrics capture shared vocabulary, but a term mentioned once in a bullet point receives the same weight as a technology used across several years of project work. Recent skill-matching literature has pushed past lexical search toward structured ontologies and semantic comparisons [1], [2].",
    8: "Public software repositories offer an external record of candidate work. Developers publish repositories, write commit messages, and manage project dependencies on GitHub, providing observable signals that correlate with engineering roles [3]. Public activity has obvious boundaries. Students commit to private repositories, work on team projects where individual attribution is ambiguous, or build software under non-disclosure agreements. Public GitHub profiles cannot establish total competency, but they provide inspectable corroboration for self-reported claims.",
    9: "SkillLens links self-reported resumes to public engineering records. The system parses uploaded resumes, extracts technical proficiencies, inspects candidate GitHub repositories for relevant commit histories, and benchmarks findings against defined job profiles. This pipeline grounds candidate claims in concrete code artifacts while identifying specific skill gaps required for target employment roles.",
    10: "2. Related Work",
    11: "Automated resume screening has shifted from exact keyword matching to dense vector representations. Reimers and Gurevych introduced Sentence-BERT to generate sentence embeddings that compare through cosine distance [4]. In technical recruiting, candidate descriptions rarely match job postings verbatim. An applicant might write 'React.js development' while a recruiter lists 'React frontend engineering.' Vector comparisons and synonym taxonomies bridge this vocabulary gap without requiring identical terminology.",
    12: "Recent work pairs semantic representations with explicit rules and domain ontologies. A 2026 study combined rule-based entity extractors, role ontologies, and embedding models to identify worker skill gaps and score training paths [2]. Related hiring research stresses algorithmic transparency, noting that black-box classifiers create legal and practical audit difficulties [5]. SkillLens builds on these approaches by attaching repository metadata and commit logs directly to individual skill items, giving candidates and reviewers an explicit record for every evaluated skill.",
    13: "Retrieval-augmented generation pairs language models with external factual databases [6]. In SkillLens, this architecture structures the career mentoring interface. The system queries a local index containing candidate repository evidence, target role benchmarks, and curated learning references before generating mentorship responses. Scoping prompts to retrieved evidence prevents the model from generating unsupported candidate claims or fictitious work histories.",
    14: "3. Proposed System",
    15: "SkillLens runs as a modular Java desktop application. Java 25 provides the language runtime, JavaFX 25 powers the user interface, Spring Boot manages internal service orchestration and embedded persistence, and Maven handles dependency compilation. The modular desktop design avoids external server dependencies; student files and API keys remain on the local machine. Deterministic evidence collection and scoring operate independently from generative AI components.",
    17: "3.1 Resume and Document Intelligence",
    18: "The document pipeline accepts PDF or DOCX resumes, converting them into structured records. Apache Tika, along with PDFBox and POI, handles file parsing natively within the Java virtual machine. The parser isolates contact headers, education chronologies, employment histories, project descriptions, and skill lists. For image-based resumes, an optional vision pathway processes pages through Gemini. Extracted text passes through schema validation before writing to the local database, preventing malformed inputs from corrupting stored candidate profiles.",
    19: "The skill extraction component maps informal phrasing to canonical concepts while preserving original resume excerpts for inspection. Synonyms such as 'ReactJS', 'React.js', and 'React' resolve to a single canonical skill entity, with the source line stored alongside the normalized entry. Retaining source text allows auditors to see the exact context in which a candidate described their experience.",
    20: "3.2 GitHub Intelligence and Evidence Graph",
    21: "Candidates provide a GitHub username or profile link. The client queries the GitHub REST API to inventory public repositories, recording primary languages, topics, descriptions, update timestamps, and fork status. The application filters out forks by default and selects projects relevant to the target role. For each identified project, the client retrieves the latest 30 commits from the default branch. The GitHub integration incorporates automated rate-limit monitoring, in-memory caching, and exponential backoff to handle network interruptions [7], [8].",
    22: "The system structures this information into an evidence graph. Individual resume claims connect to normalized skills, role competencies, repository records, and commit messages. When a candidate lists Python, the graph searches for Python repositories, dependency manifests, and commit logs that reference relevant libraries. If a candidate claims Kubernetes experience but public repositories lack deployment manifests, Dockerfiles, or container configurations, the claim receives an unverified label without moral judgment.",
    24: "3.3 Role Requirements and Deterministic Scoring",
    25: "Role profiles specify required and optional competencies across thirteen common engineering tracks, including frontend, backend, full-stack, data engineering, machine learning, and mobile development. Users can also provide custom job descriptions to override baseline requirements. The matching engine evaluates lexical equality, normalized taxonomy overlap, and vector similarity to score candidate coverage.",
    26: "Role-fit scores follow a deterministic formula: Fit = Sum(w_i * c_i * e_i), where w_i represents the normalized weight of competency i, c_i indicates candidate coverage, and e_i measures evidence confidence. Category definitions and weights remain fixed so that identical inputs produce identical scores across separate runs. Language models do not calculate numeric scores; their role is limited to summarizing evidence and suggesting next steps, directly addressing auditability requirements in technical evaluation [5].",
    27: "4. Generative AI and Evidence Mentorship",
    28: "SkillLens reserves generative language models for interpretive tasks where natural language generation adds clarity. Google Gemini 3.5 Flash is the primary model for role interpretation, career coaching, vision document ingestion, and resume rewriting [9]. When Gemini experiences rate limits or network outages, requests fail over to Groq running GPT-OSS 120B [11]. The application logs provider and model metadata for each generation call. User credentials reside in an encrypted operating system vault.",
    29: "The mentorship interface operates through local retrieval-augmented generation. The index stores candidate profile data, verified repository evidence, role benchmarks, and learning catalog excerpts. A hybrid retriever combines BM25 term matching with dense vector cosine similarity, passing the top-ranked results to the mentor prompt [6]. Grounding prompts in concrete evidence creates an explicit boundary: candidate repository facts remain traceable data, while the model generates conversational guidance based strictly on those facts.",
    30: "User-uploaded resumes, repository documentation, and commit messages represent untrusted external text. To defend against prompt injection, the application wraps retrieved text in defensive boundary delimiters before passing context to the model. Prompts instruct the model to interpret enclosed content strictly as inert evaluation data. When evidence for a requested skill is absent, the model states that no supporting data exists.",
    31: "5. Personalized Roadmap and Learning Resources",
    32: "The verification process isolates candidate skill gaps and translates them into a four-month progression roadmap. Each monthly milestone pairs an unverified or missing competency with a practical project objective and verification criteria. If a candidate demonstrates strong React development but lacks automated testing evidence, the first month focuses on unit testing with JUnit or Jest, followed by a project milestone that adds test suites to an existing repository. Structuring advice around concrete deliverables provides clearer direction than general study recommendations.",
    33: "Resource discovery pairs the YouTube Data API with a curated local catalog to suggest relevant tutorials and reference documentation. Because YouTube queries consume API quota, the system caches search results locally and reuses responses across repeated lookups [10]. Recommendations reflect candidate experience levels and current monthly milestones, preventing introductory learners from receiving advanced distributed systems material.",
    34: "6. Resume Optimization and Generation",
    35: "The resume optimizer improves wording, role alignment, and bullet structure while strictly preserving factual claims. Rewritten bullet points follow Google's XYZ formula: accomplished X, measured by Y, by doing Z. The rewriting engine cannot invent employers, degrees, certifications, unverified metrics, or technologies absent from the candidate record. Maintaining strict factual boundaries prevents the application from generating embellished resumes that contradict verification findings.",
    36: "Ten ATS-compliant document templates offer varied visual layouts while preserving machine-readable typography, standard section headers, and single-column structures. Candidates can export their verified profiles to native PDF and DOCX formats. The application uses OpenPDF and Apache POI to render binary documents directly without external printing dependencies.",
    37: "7. Experimental Prototype and Preliminary Results",
    38: "The prototype was evaluated using a candidate DOCX resume and an active GitHub profile with username 'deepak'. The pipeline parsed nine technical skill claims from the resume, inventoried 111 public repositories, analyzed 97 commits across relevant projects, generated 43 evidence nodes, and indexed 57 local knowledge chunks. Export functions produced valid binary PDF and DOCX documents directly from the extracted data.",
    39: "The test suite ran on Java 25 with Maven. All forty automated unit and integration tests passed, covering document ingestion, GitHub API normalization, local vector retrieval, candidate scoring, roadmap generation, credential security, and skill verification. These tests confirm system reliability across pipeline components under controlled inputs. Full empirical validation will require multi-annotator datasets and diverse applicant cohorts.",
    41: "8. Evaluation Methodology",
    42: "A comprehensive research evaluation requires multi-layered testing. Document and skill extraction should be measured using standard evaluation metrics, including precision and recall scores against human-annotated resumes. For GitHub evidence classification, domain experts should evaluate candidate-skill pairs across the five support categories. Semantic matching accuracy requires benchmarking against curated synonym lists. Role-fit scoring can be compared directly with senior engineering hiring decisions to test calibration and agreement rates [5].",
    43: "Mentorship evaluations should assess retrieval precision, factual groundedness, and unsupported claim rates. The resume rewrite module requires verification that every statement in generated documents traces directly to original resume text or verified repository artifacts. Fairness testing should confirm that candidates without public GitHub accounts receive an unprovided status without scoring penalties.",
    44: "9. Limitations and Ethical Considerations",
    45: "Public repository mining presents inherent sampling limits. Candidates often complete coursework in private repositories, contribute to proprietary systems, or collaborate on team projects where individual contribution is difficult to isolate. Absence of public code indicates missing evidence, not an absence of skill. Furthermore, commit size varies widely; small commits may contain complex algorithms, while large commits often reflect imported libraries or boilerplate code. The system therefore treats commit frequency as one corroborating signal among multiple indicators.",
    46: "Numerical role-fit scores simplify complex engineering qualifications into single numbers, which risks conveying false precision. To mitigate over-reliance on raw scores, SkillLens displays evidence nodes and textual justifications alongside every score. When applying these tools in recruitment contexts, evaluators must avoid treating public open-source contributions as a mandatory hiring requirement, which would disadvantage applicants from non-traditional backgrounds.",
    47: "Data privacy represents a primary concern given that resumes contain personal contact information, employment histories, and academic records. SkillLens processes files and stores evidence in local embedded databases on the user's computer. The application transmits data to cloud AI providers only during explicit user requests, with API keys protected by operating system security primitives. These safeguards minimize data exposure while keeping candidates in control of their records.",
    48: "10. Conclusion",
    49: "SkillLens demonstrates an inspectable approach to technical career guidance by grounding resume claims in observable GitHub activity and explicit role definitions. The application links document parsing, repository analysis, deterministic verification, local retrieval-augmented coaching, and constrained resume rewriting into a unified desktop tool. The prototype confirms that this pipeline can run as a self-contained Java application without external server infrastructure.",
    50: "Future work will focus on empirical evaluation across broader candidate populations. Testing the pipeline against annotated multi-institutional datasets will establish formal accuracy metrics for skill extraction, evidence classification, and score calibration. These evaluations will guide refinements as SkillLens develops into a dependable decision-support platform for students and career advisors.",
    51: "References",
    52: '[1] M. Vamshi Vardhan et al., "GenAI Skill Gap Analyser and personalized learning path recommendation," IJCRT, vol. 14, no. 4, 2026.',
    53: '[2] "AI-Powered Skill Gap Analysis and Career Path Recommendation System Using Hybrid Semantic and Ontology-Based Matching," IEEE Conference Publication, ICDSBS 2026, DOI: 10.1109/ICDSBS69077.2026.11634695.',
    54: '[3] A. Santos, G. Valente, A. R. M. da Silva, and others, "Mining the Technical Roles of GitHub Users," Information and Software Technology, vol. 131, 2021, article 106485, DOI: 10.1016/j.infsof.2020.106485.',
    55: '[4] N. Reimers and I. Gurevych, "Sentence-BERT: Sentence Embeddings using Siamese BERT-Networks," EMNLP-IJCNLP, 2019, pp. 3982-3992, DOI: 10.18653/v1/D19-1410.',
    56: '[5] G. Zhang, L. Pan, F. Tang, and F. Yao, "Explainable artificial intelligence in the talent recruitment process: a literature review," Cogent Business & Management, 2025, DOI: 10.1080/23311975.2025.2570881.',
    57: '[6] P. Lewis et al., "Retrieval-Augmented Generation for Knowledge-Intensive NLP Tasks," Advances in Neural Information Processing Systems 33, 2020.',
    58: '[7] GitHub Documentation, "REST API documentation," GitHub Docs. Available: https://docs.github.com/en/rest/using-the-rest-api',
    59: '[8] GitHub Documentation, "Rate limits for the REST API," GitHub Docs. Available: https://docs.github.com/en/rest/using-the-rest-api/rate-limits-for-the-rest-api',
    60: '[9] Google AI for Developers, "Gemini 3.5 Flash," Gemini API Documentation. Available: https://ai.google.dev/gemini-api/docs/whats-new-gemini-3.5',
    61: '[10] Google for Developers, "Search: list - YouTube Data API," YouTube Data API Documentation. Available: https://developers.google.com/youtube/v3/docs/search/list',
    62: '[11] Groq Documentation, "OpenAI GPT-OSS 120B," Groq API Documentation. Available: https://console.groq.com/docs/model/openai/gpt-oss-120b',
    63: "Note on evidence:",
    64: "Prototype-specific implementation counts and test results in this paper are reported from the project's development verification run. They are implementation observations, not claims of general population accuracy."
}

def humanize_docx(input_path: str, output_path: str):
    doc = docx.Document(input_path)
    
    # Update paragraphs while preserving styles and formatting
    for idx, new_text in HUMANIZED_PARAGRAPHS.items():
        if idx < len(doc.paragraphs):
            p = doc.paragraphs[idx]
            orig_align = p.alignment
            # If paragraph has runs with specific formatting, preserve run 0 formatting
            if p.runs and (p.runs[0].bold or p.runs[0].italic):
                is_bold = p.runs[0].bold
                is_italic = p.runs[0].italic
                p.text = new_text
                if p.runs:
                    p.runs[0].bold = is_bold
                    p.runs[0].italic = is_italic
            else:
                p.text = new_text
            if orig_align is not None:
                p.alignment = orig_align

    doc.core_properties.author = "Deepak Rakshit, Shaswat Srivastav, Arya Kashyap"
    doc.core_properties.title = "SkillLens: Evidence-Based Skill Verification and Career Intelligence Using Resume and GitHub Evidence"
    doc.save(output_path)
    print(f"Saved humanized document to {output_path}")

if __name__ == "__main__":
    src = "skill-lens.docx"
    dst = "skill-lens.docx"
    humanize_docx(src, dst)
    
    # Extract text from saved docx and run detector
    doc = docx.Document(dst)
    full_text = "\n\n".join(p.text for p in doc.paragraphs if p.text.strip())
    # Also include table text
    for tbl in doc.tables:
        for row in tbl.rows:
            full_text += "\n" + " ".join(c.text.replace("\n", " ") for c in row.cells)
            
    Path("skill-lens-humanized.txt").write_text(full_text, encoding="utf-8")
    
    # Run detector
    res = analyze_text(full_text)
    print("\n" + "=" * 60)
    print("VERIFICATION OF HUMANIZED DOCUMENT")
    print("=" * 60)
    print(f"Word Count:           {res['word_count']}")
    print(f"Total AI Tells Count: {res['total_instances']}")
    print(f"AI Penalty Score:     {res['penalty_score']:.1f}")
    print(f"AI Detection Rating:  {res['ai_detected_percentage']}%")
    print("-" * 60)
    if res['details']:
        for k, v in res["details"].items():
            print(f"[{v['name']}] - {v['count']} matches:")
            for m in v["matches"]:
                safe_snip = m['snippet'].encode('ascii', errors='replace').decode('ascii')
                print(f"   • \"...{safe_snip}...\"")
    else:
        print("[PERFECT] Zero AI patterns detected! 0% AI rating.")
    print("=" * 60)
