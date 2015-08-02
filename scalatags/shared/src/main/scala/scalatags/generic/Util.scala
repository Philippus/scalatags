package scalatags
package generic
import acyclic.file

import scalatags._

/**
 * Created by haoyi on 6/2/14.
 */
trait Util[Builder, Output <: FragT, FragT] extends LowPriUtil[Builder, Output, FragT]{

  type ConcreteHtmlTag[T <: Output] <: TypedTag[Builder, T, FragT]
  def makeAbstractTypedTag[T <: Output](tag: String, void: Boolean, namespaceConfig: Namespace): ConcreteHtmlTag[T]
  protected[this] implicit def stringAttrX: AttrValue[Builder, String]
  protected[this] implicit def stringStyleX: StyleValue[Builder, String]
  protected[this] implicit def stringPixelStyleX: PixelStyleValue[Builder, String]

  /**
   * Provides extension methods on strings to fit them into Scalatag fragments.
   */
  implicit class ExtendedString(s: String){
    /**
     * Converts the string to a [[ConcreteHtmlTag]]
     */
    def tag[T <: Output](implicit namespaceConfig: Namespace) = {
      if (!Escaping.validTag(s))
        throw new IllegalArgumentException(
          s"Illegal tag name: $s is not a valid XML tag name"
        )
      makeAbstractTypedTag[T](s, false, namespaceConfig)
    }
    /**
     * Converts the string to a void [[ConcreteHtmlTag]]; that means that they cannot
     * contain any content, and can be rendered as self-closing tags.
     */
    def voidTag[T <: Output](implicit namespaceConfig: Namespace) = {
      if (!Escaping.validTag(s))
        throw new IllegalArgumentException(
          s"Illegal tag name: $s is not a valid XML tag name"
        )
      makeAbstractTypedTag[T](s, true, namespaceConfig)
    }
    /**
     * Converts the string to a [[Attr]]
     */
    def attr = {
      if (!Escaping.validAttrName(s))
        throw new IllegalArgumentException(
          s"Illegal attribute name: $s is not a valid XML attribute name"
        )
      Attr(s)
    }
    /**
     *  Converts a string to an empty [[Attr]]. An empty attribute in HTML 
     *  is one that doesn't have an have a value (often represents a boolean
     *  in HTML)
     */
    def emptyAttr = {
      s.attr:=s
    }
    /**
     * Converts the string to a [[Style]]. The string is used as the cssName of the
     * style, and the jsName of the style is generated by converted the dashes
     * to camelcase.
     */
    def style = Style(camelCase(s), s)

  }


  /**
   * Allows you to modify a [[ConcreteHtmlTag]] by adding a Seq containing other nest-able
   * objects to its list of children.
   */
  implicit class SeqNode[A <% Modifier[Builder]](xs: Seq[A]) extends Modifier[Builder]{
    def applyTo(t: Builder) = xs.foreach(_.applyTo(t))
  }

  /**
   * Allows you to modify a [[ConcreteHtmlTag]] by adding an Option containing other nest-able
   * objects to its list of children.
   */
  implicit def OptionNode[A <% Modifier[Builder]](xs: Option[A]) = new SeqNode(xs.toSeq)

  /**
   * Allows you to modify a [[ConcreteHtmlTag]] by adding an Array containing other nest-able
   * objects to its list of children.
   */
  implicit def ArrayNode[A <% Modifier[Builder]](xs: Array[A]) = new SeqNode[A](xs.toSeq)


}

trait LowPriUtil[Builder, Output <: FragT, FragT]{
  /**
   * Renders an Seq of [[FragT]] into a single [[FragT]]
   */
  implicit def SeqFrag[A <% Frag[Builder, FragT]](xs: Seq[A]): Frag[Builder, FragT]

  /**
   * Renders an Option of [[FragT]] into a single [[FragT]]
   */
  implicit def OptionFrag[A <% Frag[Builder, FragT]](xs: Option[A]) = SeqFrag(xs.toSeq)

  /**
   * Renders an Seq of [[FragT]] into a single [[FragT]]
   */
  implicit def ArrayFrag[A <% Frag[Builder, FragT]](xs: Array[A]) = SeqFrag[A](xs.toSeq)

  /**
   * Lets you put Unit into a scalatags tree, as a no-op.
   */
  implicit def UnitFrag(u: Unit): Frag[Builder, FragT]
}
