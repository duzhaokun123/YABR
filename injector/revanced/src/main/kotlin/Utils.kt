internal fun org.w3c.dom.Document.getNode(tagName: String) = this.getElementsByTagName(tagName).item(0)

internal fun org.w3c.dom.Element.getNode(tagName: String) = this.getElementsByTagName(tagName).item(0)
