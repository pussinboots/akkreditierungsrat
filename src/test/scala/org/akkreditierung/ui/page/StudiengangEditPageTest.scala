package org.akkreditierung.ui.page

import org.specs2.mutable._
import org.apache.wicket.util.tester.{FormTester, WicketTester}
import org.akkreditierung.model.{Studiengang, StudiengangAttribute}
import org.akkreditierung.test.HSQLDbBefore
import org.akkreditierung.ui.WicketApplication

class StudiengangEditPageTest extends Specification with HSQLDbBefore {

  def setValue(form: FormTester, index:Int, tuple: (String, String)) {
    form.setValue(s"displayPanel:${index}:value_column", tuple._2)
  }

  "StudiengangEditPage" should {
    "authenticated access is needed" in {
      val wt = new WicketTester(new WicketApplication())
      wt.startPage(classOf[StudiengangEditPage])
      wt.assertRenderedPage(classOf[MySignInPage])
      wt.newFormTester("signInPanel:signInForm")
        .setValue("username", "wicket")
        .setValue("password", "wicket")
        .submit()

      wt.assertRenderedPage(classOf[StudiengangEditPage])
      () must be equalTo("did something")
    }

    "insert a new studiengang" in {
      val wt = new WicketTester(new WicketApplication())
      wt.startPage(classOf[StudiengangEditPage])
      wt.assertRenderedPage(classOf[MySignInPage])
      wt.newFormTester("signInPanel:signInForm")
        .setValue("username", "wicket")
        .setValue("password", "wicket")
        .submit()
      val attribueMap = Seq("Abschlussgrad"->"Bachelor of Science",
        "Akkreditiert"->"Ja",
        "Akkreditiert bis"->"30.9.2015",
        "Auflagen"->"Die Auflagen wurden von der Akkreditierungskommission am 11.05.2012 als erfüllt bewertet.",
        "Auflagen erfüllt"->"Ja",
        "Besondere Studienform"->"Teilzeitstudium",
        "E-mail"->"info@medicalschool-hamburg.de",
        "Erstakkreditierung"->"Bachelor of Science",
        "Fakultät / Fachbereich"->"Fakultät Gesundheit",
        "Fax"->"040/ 36 12 26 430",
        "Gutachten Link"->"http://ahpgs.de/wp-content/uploads/2012/07/Gutachten_Berlin_MSB_BA_ANP.pdf",
        "Hochschule"->"MSH Medical School Hamburg - University of Applied Sciences and Medical University",
        "Kontaktperson"->"Ilona Renken@Olthoff",
        "Mitglieder der Gutachtergruppe"->"Prof. Dr. Thomas Bals, Universität Osnabrück, Fachbereich Erziehungs- und Kulturwissenschaften, Institut für Erziehungswissenschaft. Prof. Christel Bienstein, Universität Witten-Herdecke, Institut für Pflegewissenschaft. Prof. Dr. Bernhard Borgetto, HAWK Hochschule für Angewandte Wissenschaft und Kunst - Fachhochschule Hildesheim / Holzminden / Göttingen, Standort Hildesheim, Fakultät Soziale Arbeit und Gesundheit. Prof. Dr. Sylvia Kägi, Evangelische Hochschule Ludwigsburg. Prof. Dr. Jürgen Kühl, Kinderarzt (Hochschullehrer in Pension). Prof. Dr. Christoph Steinebach, ZHAW Zürcher Hochschule für Angewandte Wissenschaften, Departement Angewandte Psychologie. Oswald Hartwick, Universitätsklinikum Schleswig-Holstein, Kiel (Leiter Dezernat Personal als als Vertreter der Berufspraxis). Susanne Max, Studierende an der HAWK Hochschule für Angewandte Wissenschaft und Kunst - Fachhochschule Hildesheim / Holzminden / Göttingen, Standort Hildesheim, Fakultät Soziale Arbeit und Gesundheit (Vertreterin der Studierenden) Sarah Rubsamen, Studierende an der Katholischen Fachhochschule Freiburg (Vertreterin der Studierenden)",
        "Profil des Studiengangs"->"Der BA-Studiengang ?Advanced Nursing Practice?, der von der Fakultät Gesundheit angeboten wird, ist ein grundständiger Bachelor-Studiengang, in dem insgesamt 180 ECTS-Anrechnungspunkte nach dem ?European Credit Transfer System? vergeben werden. Der Studiengang, der sich an examiniertes Pflegepersonal mit Berufserfahrung richtet, wird als ein neun Semester umfassendes Teilzeitstudium angeboten. Studierende, die als Zugangsvoraussetzung für das Studium eine Hochschulzulassungsberechtigung (mindestens Fachhochschulreife) und eine erfolgreich abgeschlossene einschlägige Berufsausbildung nachweisen, können durch eine erfolgreich absolvierte ?Einstufungsprüfung? bis zu 60 ECTS auf das Studium anrechnen lassen. Der Studiengang bietet die Möglichkeit, einen akademischen Abschluss mit erweiterten Fachkompetenzen in den Bereichen Intensivmedizin/Intensivpflege und/oder Anästhesiologie/ Anästhesiepflege zu erwerben (es ist nicht vorgesehen, dass Pflegende mit einem BA-Abschluss ärztliche Vorbehaltstätigkeiten übernehmen). Das Studium wird mit dem Hochschulgrad ?Bachelor of Science? (B.Sc.) abgeschlossen. Zulassungsvoraussetzung für das Studium ist die allgemeine Hochschulreife, die fachgebundene Hochschulreife oder die Fachhochschulreife und eine abgeschlossene Berufausbildung in der Pflege. Daneben ist ein besonderer Zugang für Personen ohne schulische Hochschulzugangsberechtigung gemäß § 38 HambHG möglich. Dem Studiengang stehen 25 Studienplätze pro Jahr zur Verfügung. Die Zulassung erfolgt jeweils zum Wintersemester. Die erstmalige Immatrikulation von Studierenden erfolgte im Wintersemester 2011/2012.",
        "Regelstudienzeit"->"9 Semester",
        "Studienfach"->"Advanced Nursing Practice",
        "Telefon"->"040/ 36 12 26 40",
        "von"->"Akkreditierungsagentur im Bereich Gesundheit und Soziales (AHPGS)",
        "Weitere Informationen"->"Zusätzliche Angaben zu diesem Studiengang finden Sie im Hochschulkompass der HRK.",
        "www"->"http://www.medicalschool-hamburg.de",
        "Zusammenfassende Bewertung"->"Der BA-Studiengang ?Advanced Nursing Practice?, der sich an examinierte Pflegekräfte richtet, die einen akademischen Abschluss und ein vertieftes Wissen in den Gebieten ?Intensivmedizin / Intensivpflege?, ?Anästhesie / Anästhesiepflege? oder ?Notfallmedizin und Notfallmanagement? anstreben, grenzt sich mit den genannten Schwerpunkten in besonderem Maße von den bestehenden Studienangeboten ab.")
      wt.assertRenderedPage(classOf[StudiengangEditPage])
      val form = wt.newFormTester("form")
      form.setValue("fach", "Advanced Nursing Practice")
        .setValue("abschluss", "Bachelor/Bakkalaureus")
        .setValue("hochschule", "Hamburg MSH")
        .setValue("bezugstyp", "Studienmöglichkeiten (grundständig)")
        .setValue("gutachtenLink", "http://ahpgs.de/wp-content/uploads/2012/07/Gutachten_Berlin_MSB_BA_ANP.pdf")
      for((tuple,i) <- attribueMap.zipWithIndex) {
        setValue(form,i, tuple)
      }
      form.submit()
      val studienGang = Studiengang.findByFach("Advanced Nursing Practice")
      val studienGangAttribute = StudiengangAttribute.find(studienGang)
      studienGang.fach must be equalTo("Advanced Nursing Practice")
      studienGang.hochschule must be equalTo("Hamburg MSH")
      studienGang.gutachtentLink must be equalTo(Some("http://ahpgs.de/wp-content/uploads/2012/07/Gutachten_Berlin_MSB_BA_ANP.pdf"))

      for ((k, v) <- attribueMap) {
        studienGangAttribute(k).value must beEqualTo(v)
      }
      () must be equalTo("did something")
    }
  }
}
