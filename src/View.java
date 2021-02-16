


/*
 * HtmlView.java
 *
 * MIDP "Pico" Web Browser Framework
 * 
 * (C) 2001 Beartronics Inc.
 * Author: Henry Minsky (hqm@alum.mit.edu)
 *
 * Licensed under terms "Artistic License"
 * http://www.opensource.org/licenses/artistic-license.html
 *
 * $Id: HtmlView.java,v 1.16 2001/07/02 06:46:37 hqm Exp $
 *
 * Changed for low resources by Jaromi Skubala as part his project Dictionary
 *
 *  !!!! Very very lite version but very low memory and access memory print
 *
 *
 */


import java.util.Vector;
import javax.microedition.lcdui.Font;
import javax.microedition.lcdui.Graphics;
import javax.microedition.lcdui.Image;

public class View {
	/**
	 *  Html tags constatnt
	 */
	public static final String CAMP = "amp";
	static final String CLT = "lt";
	static final String CGT = "gt";
	static final String CQUOT = "quot";
	static final String CCOPY = "copy";
	static final String CNBSP = "nbsp";

	static final String CBR = "br";
	static final String CP = "p";
	static final String CSTRONG = "strong";
	static final String CB = "b";
	static final String CA = "a";
	static final String CPRE = "pre";
	static final String CCENTER = "center";
	static final String CDIV = "div";
	static final String CHR = "hr";
	static final String CDD = "dd";
	static final String CLI = "li";
	static final String CDL = "dl";
	static final String CDT = "dt";
	static final String CFONT = "font";
	static final String CTITLE = "title";
	static final String CEM = "em";
	static final String CI = "i";
	static final String CIMG = "img";
	static final String CBASE = "base";
	static final String CBLOCKQUOTE = "blockquote";
	static final String CUL = "ul";
	static final String CHREF = "href";
	static final String CALIGN = "align";
	static final String CCOLOR = "color";
	static final String CSRC = "src";
	static final String C123456 = "123456";

	// constatnts for html parsing
	static final int TEXT = 0; 
	static final int TAG  = 1; 
	static final int ATTR_WHITESPACE  = 0;
	static final int ATTR_NAME        = 1;
	static final int ATTR_EQUALS      = 2;
	static final int VALUE_WHITESPACE = 3;
	static final int ATTR_VALUE       = 4;
	static final int ATTR_VALUE_QUOTE = 5;
	static final int CHECK_MATCH      = 6;

	int width;
	int height;
	int left;
	int top;

	int textColor;
	/** 
	 * If true, try to wrap words at whitespace or punctuation. If false,
	 * wrap words at right margin, regardless of whitespace.
	 */
	public boolean wordWrap = true;

	/*
        selected index of the item which is hyperlink can be text of image starts zero
	 */
	public int selectedLinkId = -1;

	/** 
        if is it set the text hyperlinks are undrlined
	 */
	public boolean linkIsUnderlined = true;

	/**
	 *
	 * Just private properties
	 *
	 */

	/** that where elements of the page are stored each element is procesed ant the
	 *have some reslution and position which is determinet during a drawing and paint it
	 */
	public Vector items = new Vector();

	// during layout proces is counter of items then is holding count of links in a page
	private int maxLinkId;


	//step of page down page up
	private int pageSize;

	//step of line downUp
	private int lineStep;

	/** The maximum Y height of the virtual page. */
	public int maxHeight = 0;

	/** y position of current "viewpoint" into current page. */
	public int  currentPosY = 0;


	/** The screen right margin */
	int lineWidth;

	/** How much to indext UL or BLOCKQUOTE */
	public int xIndent = 10;

	/** 
	 * The URL we are currently pointing to. 
	 * This can be overriden by the BASE tag in a document.
	 */
	String currentDocumentBase = null;

	/**
	 * vars used only during a layoot process
	 *
	 */
	private Font lsFont; //holdu=ing the font info
	private boolean lsIsLink; //holding the info if next elements are links
	private String lsLink; // value of the link
	private int lsColor; // 
	private int lsPrevColor;
	private boolean lsPreformat; // preserve whitespace and newlines
	private boolean lsCenter; // display text centered on the screen?
	private boolean lsRunningWhitespace;
	private int lsLeftMargin;
	private int lsRightMargin;
	private int lsXPos;
	private int lsYPos;
	// we do not holding info about a line in elements so during a layout we need hold info which
	// lements are on the line for adjustment purpose
	private HtmlItem [] lineItems = new HtmlItem[100];
	private int [] itemAlignment = new int[lineItems.length];
	// how many elements on the line
	private int lineItemCount = 0;

	// holding the info about the text elemnt can be tag or text
	private int type;
	// is it closing tag ??
	private boolean leadingSlash = false;

	// utily buffers
	private Bytes tag = new Bytes(100);

	/*
	 *The raw substring which contains the attributes or text content i.e., 
	 * 'foo=3 bar="baz baz baz"'
	 */
	private Bytes value = new Bytes(100);

	//utily buffer forr atributes in tag
	private Bytes attrname = new Bytes(100);
	private Bytes attrvalue = new Bytes(100);


	public View(int x, int y, int w, int h) {
		left = x;
		top = y;
		width = w;
		height = h;
		lineWidth = width;
	}


	/**
	 *     procedures for html text parsing
	 *
	 */

	// read html raw text and deside if the element is text or tag
	// retun the result in field type
	// if text in field value is the currnet text else in value is the atributes of the tag
	// and field tag is used for a tag

        private int readNextElement (Bytes text, int idx) {
		int maxidx = text.size();

		char ch = text.charAt(idx);
		char nextchar;

		// one character lookahead, watch out for end of buffer.
		if (idx == (maxidx-1)) {
			nextchar = ' ';
		} else {
			nextchar = text.charAt(idx+1);
		}
		// Look for a tag in <>'s
		if ((ch == '<') && (!Bytes.isWhitespace(nextchar))) {
			idx++;

			// look for end of tag
			int tagend = text.indexOf('>', idx);

			// If we've got a badly formed tag at the end of the buffer, 
			// just try to do the best we can.
			if (tagend == -1) {
				tagend = maxidx-1;
			}

			// Grab the tag name

			// first whitespace after the tag.
			int whitespace = text.indexOf(' ', idx);

			// index of the end of the tag name
			int eltEnd;

			// start of the attributes text
			int attrStart;

			if (whitespace >= 0) {
				eltEnd = (whitespace < tagend ? whitespace : tagend);
			} else {
				eltEnd = tagend;
			}

			if (whitespace >= 0) {
				attrStart = (whitespace+1 < tagend ? whitespace+1 : tagend);
			} else {
				attrStart = tagend;
			}

			// We need to preserve case of tags for XHTML.

			// Is it a closing tag?
			if (text.charAt(idx) == '/') {
				leadingSlash = true;
				idx++;
			} else {
				leadingSlash = false;
			}

			type = TAG;
			tag.erase();
			tag.append(text,idx, eltEnd);
			value.erase();
			value.append(text, attrStart, tagend);

			if ((tagend + 1) >= maxidx) {
				return maxidx;
			} else {
				return tagend + 1;
			}
		} else {
			// Look for next occurence of '<' marking a new tag. 

			// Clear the string buffer
			value.erase();

			while (idx < maxidx) {
				ch = text.charAt(idx++);

				// Look for HTML entities of the form &xxx;
				if (ch == '&') {
					// Numeric entity?
					if (text.indexOf('#', idx) == 0) {
						idx++;
						// &#65;
						char val = '!';
						// search for the next non-numeric char
						int term;
						char cterm = 0;
						for (term = idx; term < maxidx; term++) {
							cterm = text.charAt(term);
							if (!Character.isDigit((char)cterm)) {
								break;
							}
						}
						try {
							val = (char) (Integer.parseInt(text.substring(idx,term).toString()));
						} catch (Exception e) {
						}
						value.append(val);
						if (cterm == ';') {
							idx = term+1;
						} else {
							idx = term;
						}
					} else {
						// Symbolic entity (&nbsp; &gt; etc...)
						// People are sloppy, and will forget to terminate the entities
						// with a ';', so let's be nice and also terminate at the first whitespace.
						char cterm = 0;
						int term;
						// Scan for non-alpha char
						for (term = idx; term < maxidx; term++) {
							cterm = text.charAt(term);
							if (!Bytes.isLetter(cterm)) {
								break;
							}
						}

						if (text.regionMatches(idx, CAMP)) {
							value.append('&');
						} else if (text.regionMatches(idx, CLT)) {
							value.append('<');
						} else if (text.regionMatches(idx, CGT)) {
							value.append('>');
						} else if (text.regionMatches(idx, CQUOT)) {
							value.append('\"');
						} else if (text.regionMatches(idx, CCOPY)) {
							value.append((char)169);
						} else if (text.regionMatches(idx, CNBSP)) {
							// We should handle nonbreaking space differently, but we'll
							// just treat it is regular space for now. 
							value.append(' ');
						}

						// Move on past end of entity

						// If it was properly terminated with a semicolon, eat the terminator
						if (cterm == ';') {
							idx = term+1;
						} else {
							idx = term;
						}
					}
				} else if (ch == '<') {
					if (idx == (maxidx-1)) {
						nextchar = ' ';
					} else {
						nextchar = text.charAt(idx);
					}

					if (!Bytes.isWhitespace(nextchar)) {
						// A real tag is starting we're done, return this element,
						// back the index up one to where the '<' is.
						idx--;
						break;
					}
				} else {
					// add to the buffer
					value.append(ch);
				}
			}

			type = TEXT;
			tag.erase();
			return idx;
		}
	}
 
	// read a tag atribute value specified by target
	// result is in field tag

        private void readAttributeVal (String target) {

		if (value == null) {
			attrvalue.erase();
			return;
		}

		int state = ATTR_WHITESPACE;

		// reinitialize result buffer
		attrvalue.erase();
		attrname.erase();
		int idx = 0;
		int len = value.size();
		char ch;

		while (idx < len) {
			switch (state) {
			case ATTR_WHITESPACE:
				// We're looking for the next attribute name.
				// Scan until we find the first non-whitespace char.
				ch = value.charAt(idx++);
				if (Bytes.isWhitespace(ch)) {
					continue;
				} else {
					// start collecting the attribute name
					attrname.append(ch);
					// clear any preexisting value
					attrvalue.erase();
					state = ATTR_NAME;
				}
				break;
			case ATTR_NAME: // gather attribute name
				// look for the first '=' or whitespace
				ch = value.charAt(idx++);
				if (Bytes.isWhitespace(ch)) {
					// whitespace found, look if an '=' occurs next
					state = ATTR_EQUALS;
				} else if (ch == '=') {
					state = VALUE_WHITESPACE;
				} else {
					attrname.append(ch);
				}
				break;
			case ATTR_EQUALS:
				// look for the '=' that may come after attribute name
				ch = value.charAt(idx++);
				if (ch == '=') {
					state = VALUE_WHITESPACE;
				} else if (!Bytes.isWhitespace(ch)) {
					// We found a non-whitespace, non-'=' character.
					// So there is no value for this attribute. We
					// should move on to check if this current
					// attribute matches the desired target attribute.

					// Unread this char, it will start the next attribute name
					idx--;

					state = CHECK_MATCH;
				}
				break;
			case VALUE_WHITESPACE:
				// Scan until non whitespace found.
				ch = value.charAt(idx++);
				if (Bytes.isWhitespace(ch)) {
					continue;
				} else {
					// Start collecting the attribute value.
					// If this starts with a double-quote, then
					// go to state ATTR_VALUE_QUOTE
					if (ch == '"') {
						state = ATTR_VALUE_QUOTE;
						//			System.out.println("VALUE_WHITESPACE going to ATTR_VALUE_QUOTE");
					} else {
						attrvalue.append(ch);
						state = ATTR_VALUE;
						//			System.out.println("VALUE_WHITESPACE going to ATTR_VALUE");
					}
				}
				break;
			case ATTR_VALUE:
				// Reading an unquoted attribute value. Look
				// for whitespace to terminate.
				ch = value.charAt(idx++);
				if (Bytes.isWhitespace(ch)) {
					state = CHECK_MATCH;
				} else {
					attrvalue.append(ch);
				}
				break;
			case ATTR_VALUE_QUOTE:
				ch = value.charAt(idx++);
				// Reading a quoted value, look for double quote
				// to terminate.
				if (ch == '"') {
					state = CHECK_MATCH;
				} else {
					attrvalue.append(ch);
				}
				break;
			case CHECK_MATCH:
				// Check if the last attribute name that we read
				// matches the desired attribute name.

				if (target.length() == attrname.size() && attrname.regionMatches(0, target)) {
					return;
				} else {
					// no match, try next attribute
					attrvalue.erase();
					attrname.erase();
					state = ATTR_WHITESPACE;
				}
				break;
			}
		}
		if (target.length() == attrname.size()  && attrname.regionMatches(0, target)) {
			return;
		} else {
			attrvalue.erase();
			return;
		}
	}

	/**
	 * Start building a new lines of the items of items.
	 *set the count of the items for zero and clear oyt the buffers
	 */
	void prepareLine () {
		lsRunningWhitespace = true;
		for(int i = 0; i < lineItemCount; i++) {
			lineItems[i] = null;
			itemAlignment[i] = Graphics.TOP;
		}
		lineItemCount = 0;
	}


	void forcedLineBreak () {
		// finish off the current line
		adjustLinePosition();

		// start a new line
		prepareLine();

		// start a new item on the next line
		lsXPos = lsLeftMargin;
	}

	/** 
	 * Only make a new line if the previous line is non-empty.
	 */
	void lineBreakIfNeeded () {
		if (lineItemCount != 0) {
			forcedLineBreak();
		}
		lsXPos = lsLeftMargin;
	}


	/** Compute various adjustments to a line after it has been filled with elements.
	 *<ul>
	 * <li> Compute total height of this line, to space it vertically from
	 * the previous line. 
	 * <li> Computer any x offset if items need to be centered.
	 * <li> Adjust vertical offsets of image items if their alignment is not BOTTOM
	 * </ul>
	 */
	void adjustLinePosition () {
		int h = 0;
		int w = 0;

		// get height of the line and width of the line
		for (int i = 0; i < lineItemCount; i++) {
			HtmlItem item = lineItems[i];
			item.ypos = lsYPos;
			h = Math.max(h, item.height);
			w += item.width;
		}

		// Only an empty line should ever have zero height. If it is
		// really empty, it was probably being used a blank space by a
		// <P> tag. Force it to be to be one char bbox height in the
		// current lsFont.
		if (h == 0) {
			h = lsFont.getHeight();
		}

		// advance virtual y pos to the next line
		lsYPos += h;
		// we need to watch size of the output
		maxHeight += h;

		// Computer centering
		if (lsCenter) {
			int deltaX = (lineWidth - w) / 2 ;
			for (int i = 0; i < lineItemCount; i++) {
				HtmlItem item =  lineItems[i];
				item.xpos += deltaX;
			}
		}

		// vertical adjutment of the items
		for (int i = 0; i < lineItemCount; i++) {
			HtmlItem item =  lineItems[i];
			if ((itemAlignment[i] & Graphics.BOTTOM) != 0) {
				if (item.height < h) item.ypos += h - item.height; 
			} else if ((itemAlignment[i] & Graphics.VCENTER) != 0) {
				if (item.height < h) item.ypos += (h - item.height) / 2; 
			} else {
				//we do not have to do anything as default layout is top align
			}
		}
	}



	/**
	 * Set the HTML text of the current page. This will parse the HTML
	 * into HtmlItem objects, and assign them layout positions on a 
	 * virtual page.
	 */

	public void preparePage() {
		//defaults
		selectedLinkId = -1;
		maxLinkId = 0;
		currentPosY = 0;
		maxHeight = 0;

		//layout states defaults
		lsFont = Font.getFont(Font.FACE_PROPORTIONAL,0, Font.SIZE_MEDIUM);
		lsIsLink = false;
		lsLink = null;
		lsColor = textColor;
		lsPrevColor = lsColor;
		lsPreformat = false;
		lsCenter = false;
		lsRunningWhitespace = false;
		lsLeftMargin = 0;
		lsRightMargin = lineWidth;
		lsXPos = 0;
		lsYPos = 0;

		lineStep = lsFont.getHeight();
		pageSize = height - lineStep;

		prepareLine();
		//dispose all elements
		items.removeAllElements();
	}
	
	public void setLayoutTextStyle(int style) {
		
	}

	public void setLayoutFont(Font f) {
		lsFont = f;
		
	}
	
	public void appendText(Bytes text) {
		if (text.empty()) return;
		layoutTextElement(text, wordWrap);
	}

	public void appendHR() {
		lineBreakIfNeeded();
		layoutHR();
		forcedLineBreak();
	}
	
	public void appendImage(Bytes image) {
		HtmlImageItem item =  new HtmlImageItem(image);
		items.addElement(item);
		if (lsIsLink) {
			item.linkId = maxLinkId;
			item.link = lsLink;
		}
		item.xpos = lsXPos;
		boolean overrun = ((lsXPos + item.width) > lsRightMargin);

		boolean emptyLine = (lineItemCount == 0);
		if (overrun && !(lsPreformat) && !emptyLine) {
			adjustLinePosition();
			prepareLine();
			lsXPos = lsLeftMargin;
			item.xpos = lsXPos;
		}
		itemAlignment[lineItemCount] = Graphics.VCENTER;
		lineItems[lineItemCount++] = item; 
		lsXPos += item.width;
	}
	
	public void layoutDt (boolean start) {
		if (start) {
			lsLeftMargin = Math.max(lsLeftMargin - xIndent, 0);
			lineBreakIfNeeded();
		} else {
			
		}
	}
	
	public void layoutDd (boolean start) {
		lineBreakIfNeeded();
		if (start) {
			lsLeftMargin = Math.min(lsLeftMargin + xIndent, lsRightMargin - xIndent);
			lineBreakIfNeeded();
		} else {
			lsLeftMargin = Math.max(lsLeftMargin - xIndent, 0);
			lineBreakIfNeeded();
		}
	}
	
	public void appendHyperlink(Bytes text, Bytes link) {
		if (text.empty()) return;
		lsIsLink = true;
		if (linkIsUnderlined && !lsFont.isUnderlined())
			lsFont = Font.getFont(lsFont.getFace(), lsFont.getStyle() | Font.STYLE_UNDERLINED, lsFont.getSize());
		if (link != null) {
			lsLink = link.toString();
		} else {
			lsLink = null;
		}
		layoutTextElement(text, wordWrap);
		lsIsLink = false;
		if (lsFont.isUnderlined())
			lsFont = Font.getFont(lsFont.getFace(), lsFont.getStyle() - Font.STYLE_UNDERLINED, lsFont.getSize());
		maxLinkId++;
	}
	
	public void commitPage() {
		// Finish off this line, space it properly from the previous line.
		adjustLinePosition();

		prepareLine();
		// We have to destroy all refernece in private properties to allow gc free the last elements
		lsFont = null;
		lsLink = null;

		// select the first hyperlink
		nextHyperlink(false);
	}
	
	public void setText (Bytes text) {
		if (text == null)
			text = new Bytes("No text...");
		preparePage();
		// current index into raw html source
		int idx = 0;
		int maxidx = text.size();


		while (idx < maxidx) {
			idx = readNextElement(text, idx);

			if (type == TEXT) {
				try {
					layoutTextElement(value, wordWrap);
				} catch (Exception ex) {
					// temporary hack to find out some errors
					this.layoutBulletElement();
//					ex.printStackTrace();
				}

			} else if (type == TAG) {

				if (tag.regionMatches(0, CBR)) {
					forcedLineBreak();
				} else if (tag.regionMatches(0, CP)) {
					forcedLineBreak();
					forcedLineBreak();
				} else if (tag.regionMatches(0, CSTRONG) || (tag.regionMatches(0, CB))) {
					// bold
					if (leadingSlash) {
						if (lsFont.isBold())
							lsFont = Font.getFont(lsFont.getFace(), lsFont.getStyle() - Font.STYLE_BOLD, lsFont.getSize());
					} else {
						if (!lsFont.isBold())
							lsFont = Font.getFont(lsFont.getFace(), lsFont.getStyle() | Font.STYLE_BOLD, lsFont.getSize());
					}
				} else if (tag.regionMatches(0, CA)) {
					// hyperlink
					if (leadingSlash) {
						lsIsLink = false;
						if (lsFont.isUnderlined())
							lsFont = Font.getFont(lsFont.getFace(), lsFont.getStyle() - Font.STYLE_UNDERLINED, lsFont.getSize());
						// increment to generate new unique ID for future links
						maxLinkId++;
					} else {
						// Look for the HREF target value. 
						lsIsLink = true;
						if (linkIsUnderlined && !lsFont.isUnderlined())
							lsFont = Font.getFont(lsFont.getFace(), lsFont.getStyle() | Font.STYLE_UNDERLINED, lsFont.getSize());
						readAttributeVal(CHREF);
						lsLink = attrvalue.toString();
						//debugPrint("A HREF="+ls.linkTarget);
					}
				} else if (((tag.charAt(0) == 'h') || (tag.charAt(0) == 'H'))
						&& (tag.size() == 2) 
						&& (C123456.indexOf(tag.charAt(1)) != -1)) {
					if (leadingSlash) {
						if (lsFont.isBold())
							lsFont = Font.getFont(lsFont.getFace(), lsFont.getStyle() - Font.STYLE_BOLD, lsFont.getSize());
					} else {
						if (!lsFont.isBold())
							lsFont = Font.getFont(lsFont.getFace(), lsFont.getStyle() | Font.STYLE_BOLD, lsFont.getSize());
					}
					lineBreakIfNeeded();
				} else if (tag.regionMatches(0, CPRE)) {
					if (leadingSlash) {
						lsPreformat = false;
						lineBreakIfNeeded();
						lsFont = Font.getFont(Font.FACE_SYSTEM, lsFont.getStyle(), lsFont.getSize());
					} else {
						forcedLineBreak();
						lsFont = Font.getFont(Font.FACE_MONOSPACE, lsFont.getStyle(), lsFont.getSize());
						lsPreformat = true;
					}
				} else if (tag.regionMatches(0, CCENTER)) {
					if (leadingSlash) {
						lineBreakIfNeeded();
						lsCenter = false;

					} else {
						lineBreakIfNeeded();
						lsCenter = true;
					}
				} else if (tag.regionMatches(0, CDIV)) {
					if (leadingSlash) {
						lineBreakIfNeeded();
						lsCenter = false;
					} else {
						lineBreakIfNeeded();
						readAttributeVal(CALIGN);
						if (attrvalue.empty() && (attrvalue.regionMatches(0, CCENTER))) {
							lsCenter = true;
						}
					}
				} else if (tag.regionMatches(0, CUL)) {
					if (leadingSlash) {
						lsLeftMargin = Math.max(lsLeftMargin - xIndent, 0);
						lineBreakIfNeeded();
					} else {
						lineBreakIfNeeded();
						lsLeftMargin = Math.min(lsLeftMargin + xIndent, lsRightMargin - xIndent);
					}
				} else if (tag.regionMatches(0, CBLOCKQUOTE)) {
					if (leadingSlash) {
						lsLeftMargin = Math.max(lsLeftMargin - xIndent, 0);
						lsRightMargin = Math.min(lsRightMargin + xIndent, lineWidth);
						lineBreakIfNeeded();
					} else {
						lsLeftMargin = Math.min(lsLeftMargin + xIndent, lsRightMargin - xIndent);
						lsRightMargin = Math.max(lsRightMargin - xIndent, lsLeftMargin + xIndent);
						lineBreakIfNeeded();

					}
				} else if (tag.regionMatches(0, CLI)) { 
					if (leadingSlash) { 
						lineBreakIfNeeded();
					} else {
						// Force a "o" item into the line 
						layoutBulletElement(); 
					} 
				} else if (tag.regionMatches(0, CHR)) {
					lineBreakIfNeeded();
					layoutHR();
					forcedLineBreak();
				} else if (tag.regionMatches(0, CDD)) {
					// This is a very hacked up way to make DD cause an indent. 
					// We push the left margin in, until we hit either a closing DD or
					// a /DL or DT tag.
					lineBreakIfNeeded();
					if (leadingSlash) {
						lsLeftMargin = Math.max(lsLeftMargin - xIndent, 0);
						lineBreakIfNeeded();
					} else {
						lsLeftMargin = Math.min(lsLeftMargin + xIndent, lsRightMargin - xIndent);
						lineBreakIfNeeded();
					}
				} else if (tag.regionMatches(0, CDL)) {
					if (leadingSlash) {
						lsLeftMargin = Math.max(lsLeftMargin - xIndent, 0);
						lineBreakIfNeeded();
					}		    
				} else if (tag.regionMatches(0, CDT)) {
					lsLeftMargin = Math.max(lsLeftMargin - xIndent, 0);
					lineBreakIfNeeded();
				} else if (tag.regionMatches(0, CFONT)) {
					if (leadingSlash) {
						lsColor = lsPrevColor;
					} else {
						// Look for the COLOR target value. 
						// Double quotes required: <FONT lsColor="#RRGGBB">
						readAttributeVal(CCOLOR);
						if (!attrvalue.empty()) {
							int r,g,b;
							r = attrvalue.parseInt(1,2,16);
							g = attrvalue.parseInt(3,4,16);
							b = attrvalue.parseInt(5,6,16);
							lsPrevColor = lsColor;
							lsColor = ( (r<<16) | (g<<8) | b );
						}
					}
				} else if (tag.regionMatches(0, CIMG)) {
					layoutImageElement();
				} else if (tag.regionMatches(0, CBASE)) {
					readAttributeVal(CHREF);
					if (attrvalue.empty()) {
						currentDocumentBase = attrvalue.toString();
					}
				} else if (tag.regionMatches(0, CTITLE)) {
					// Search for closing /TITLE tag, discard all input up to there.
					// We should actually save the title someplace, for use in
					// a bookmarks list.

					if (leadingSlash) {
						while (idx < maxidx) {
							idx = readNextElement(text, idx);
							if ((type == TAG) 
									&& (tag.regionMatches(0, CTITLE))
									&& leadingSlash) {
								break;
							}
						}
					}
				} else if (tag.regionMatches(0, CI)
						|| tag.regionMatches(0, CEM)) {
					// italic
					if (leadingSlash) {
						if (lsFont.isItalic())
							lsFont = Font.getFont(lsFont.getFace(), lsFont.getStyle() - Font.STYLE_ITALIC, lsFont.getSize());
					} else {
						if (!lsFont.isItalic())
							lsFont = Font.getFont(lsFont.getFace(), lsFont.getStyle() | Font.STYLE_ITALIC, lsFont.getSize());
					}
				}

			}
		}
		commitPage();
	}



	private void layoutHR() {
		HtmlHRulerItem item = new HtmlHRulerItem(lineWidth);
		items.addElement(item); 
		lineItems[lineItemCount++] = item; 
	}

	private void layoutBulletElement() {
		HtmlBulletItem item = new HtmlBulletItem();
		items.addElement(item);
		item.xpos = lsXPos;
		boolean overrun = ((lsXPos + item.width) > lsRightMargin);
		boolean emptyLine = (lineItemCount == 0);
		if (overrun && !(lsPreformat) && !emptyLine) {
			adjustLinePosition();
			prepareLine();

			// start a new item on the next line
			lsXPos = lsLeftMargin;
			item.xpos = lsXPos;
		}
		itemAlignment[lineItemCount] = Graphics.VCENTER; 
		lineItems[lineItemCount++] = item; 
		lsXPos += item.width;
	}

	/**
	 * Layout an image HtmlElement. Looks for the SRC and ALIGN tags
	 * 
	 * We don't handle image scaling. 
	 * 
	 * @param elt A IMAGE element
	 * @param ls the current layout state
	 */
	private void layoutImageElement() {
		readAttributeVal(CSRC);

		if (attrvalue == null) {
			return;
		}
		// Build the new image item.
		HtmlImageItem item =  new HtmlImageItem(attrvalue);
		items.addElement(item);
		if (lsIsLink) {
			item.linkId = maxLinkId;
			item.link = lsLink;
		}
		item.xpos = lsXPos;
		boolean overrun = ((lsXPos + item.width) > lsRightMargin);

		// If we have reached right screen margin, handle line wrapping, insert item
		// on a new line.

		// If this item is the only item in the line, then don't wrap, since
		// it will always be too wide no matter where we put it.

		boolean emptyLine = (lineItemCount == 0);
		if (overrun && !(lsPreformat) && !emptyLine) {
			// finish off the current line
			adjustLinePosition();

			// start a new line
			prepareLine();
			// start a new item on the next line
			lsXPos = lsLeftMargin;
			item.xpos = lsXPos;
		}
		// add item to the current line
		itemAlignment[lineItemCount] = Graphics.VCENTER;
		lineItems[lineItemCount++] = item; 
		// advance the cursor to the right side of the item.
		lsXPos += item.width;
	}

	/** 
	 * Create a new HtmlTextItem, initialize it for text, and
	 * 	add it to the current line.
	 */
	HtmlTextItem newTextItem () {
		if (tag.emty()) {
			System.out.println("nulll");
		}
		HtmlTextItem item;
		item = new HtmlTextItem(tag.toString(), lsFont, lsColor);
		items.addElement(item);
		item.xpos = lsXPos;
		if (lsIsLink) {
			item.linkId = maxLinkId;
			item.link = lsLink;
		}
		lineItems[lineItemCount++] = item;
		return item;
	}

	/**
	 * Layout a text HtmlElement. Sequentially copy text from HtmlElement into
	 * the current HtmlItem. If we reach a right margin, perform 
	 * a word-wrap. This requires adding a new HtmlViewLine to the layout and
	 * creating a new HtmlItem to continue the text on the next line.
	 * 
	 * 
	 * Note, this wraps words at the character where they hit the
	 * right margin.  To do fancier word-breaking, we should save
	 * index of the last known whitespace or punctuation.   Then we
	 * have the option of backing up to that point.
	 * 
	 * @param wordWrap if true, attempt to wrap words at whitespace boundaries
	 */

	private void layoutTextElement (Bytes value, boolean wordWrap) {
		// We may not actually need to instantiate a new HtmlItem, if
		// for example, all text is whitespace.
		int width = 0;

		HtmlTextItem item;
		//it is not tag so we can use tag buffer :)))
		tag.erase();

		// Loop over text element's buffer one char at a time,
		// typesetting into current layout item, performing wordwrap
		// if needed.


		int idx = 0;
		int maxidx = value.size();

		// The index of last whitespace in the elt's raw text
		int lastWhitespaceIdx = 0;

		// The index of the last place we inserted whitespace into the current
		// stringbuffer (not necessarily the same as lastWhitespaceIdx because
		// we are discarding redundant whitespace. 
		int lastWhitespaceBuf = 0;

		boolean wrapped = false;
		char ch;

		while (idx < maxidx) {
			// Read a character
			ch = value.charAt(idx++);	

			boolean charInserted = false;

			// Decide how to insert it.

			// Check if we are in a PRE lsPreformat block:
			if (lsPreformat) {
				// Special case - in lsPreformat text, a newline means
				// start a newline.
				if (ch == '\n') {
					if (width != 0) {
						item = newTextItem();
						width = 0;
					}
					forcedLineBreak();
					// clear out the scratch stringbuffer
					tag.erase();
				} else {
					tag.append(ch);
					charInserted = true;
				}
			} else {
				// Otherwise, layout as normal text; shrink any run of 
				// whitespace down to a single whitespace. 
				//
				// If the whitespace occurs before any printed
				// characters have been inserted on this line, keep
				// discarding whitespace until a printing character is seen.
				boolean whitespace = Bytes.isWhitespace(ch);

				if (!whitespace) {
					charInserted = true;
					tag.append(ch);
					lsRunningWhitespace = false;
				} else {
					// coerce all whitespace chars in to a ' ' char
					ch = ' ';
					if (!lsRunningWhitespace) {
						lsRunningWhitespace = true;
						//jaromes fix becouse the idx was add + 1
						lastWhitespaceIdx = idx - 1;
						lastWhitespaceBuf = tag.size(); 
						charInserted = true;
						tag.append(ch);
					}
				}
			}

			// keep track of what the width of the text string is
			if (charInserted) {
				width += lsFont.charWidth((char)ch);
				boolean overrun = ((lsXPos + width) > lsRightMargin);

				// If we have reached right screen margin, handle line
				// wrapping, start a new itme on a new line
				if (overrun && !lsPreformat) {
					//boolean wrapChar = false;

					// It's a real right-margin overrun, so pull the last char
					// off and start a new run on a new line.

					/* Actually, there are two choices - 
					 *
					 * 1) If we are in not in wordwrap mode, then just
					 * take off the last char and stick it onto the
					 * next line (unless it's whitespace).
					 *
					 * 2) In "wordWrap" mode we need to back all the
					 * way up to the last whitespace we saw on this
					 * line, (which could in theory be in an HtmlItem
					 * which is several items back on this line, but
					 * we'll finesse that and say that if it's not in
					 * this Item, we're not going to go back further
					 * than the start of this item.
					 */

					if (overrun && (tag.size() > 0)) {
						if (wordWrap) {
							// no whitespace seen, wrap the whole item to the
							// next line
							if (lastWhitespaceBuf > 0 || lineItemCount > 0) {
								tag.delete(tag.size() - lastWhitespaceBuf) ;
								lastWhitespaceBuf = 0;
								// back the char index to that point
								idx = lastWhitespaceIdx;
							} else {
								tag.delete(1);
								idx--;
							}
						} else {
							// not in wordWrap, so just strip last char
							tag.delete(1);
							// back up the pointer one char
							idx--;
						}
						wrapped = true;
					}

					// We create and typeset a new HtmlItem for this run of text
					if (tag.size() > 0)
						item = newTextItem();
					width = 0;
					tag.erase();

					/*
		      debugPrint("wrapping line. currentItem "+
		      items.indexOf(currentItem)+" x="+ currentItem.lsXPos
		      + " w="+currentItem.width
		      + " h="+currentItem.height 
		      + ": "+currentItem.text
		      ); */

					// finish off the current line
					adjustLinePosition();

					// start a new line
					prepareLine();

					// start a new item on the next line
					lsXPos = lsLeftMargin;
					// clear out the scratch stringbuffer
					tag.erase();

					// Re-read the char, since idx may have been pushed back by
					// the linewrap
					ch = value.charAt(idx++);	

					if (wrapped && !(Bytes.isWhitespace(ch))) {
						lsRunningWhitespace = false;
						tag.append(ch);
						width += lsFont.charWidth((char)ch);
					}

					//lsXPos += currentItem.width;
				} else {
					// just keep laying out characters
				}
			}
		}

		// We're finished copying chars from the HtmlElement. Finish off
		// the current item.
		if (width != 0) {
			item = newTextItem();

			// advance cursor position to right of item
			lsXPos += item.width;

			// compute final dimensions on current item
		}
	}



	/**
	 *  paint a component
	 *-------------------------------------------------------------------------
	 */
	public void paint (Graphics g) {
		//we need to clip the items which are not fully included in area so
		//we store the clip parameters and then set the clip region back
		int cx = g.getClipX(), cy = g.getClipY();
		int cw = g.getClipWidth(), ch = g.getClipHeight();
		int tranX = left ;
		int tranY = top - currentPosY;
		g.setClip(left, top, width, height);
		// translate 
		g.translate(tranX, tranY);
		for(int i = 0; i < items.size(); i++) {
			HtmlItem item = (HtmlItem) items.elementAt(i);
			if ((item.ypos >= currentPosY && item.ypos <= currentPosY + height) ||
					((item.ypos + item.height) >= currentPosY && (item.ypos + item.height) <= currentPosY + height)) {
				if (item.linkId != -1 && item.linkId == selectedLinkId) {
					item.paintSelected(g);
				} else {
					item.paint(g);
				}
			}
		}
		g.translate(0 - tranX, 0 - tranY);
		g.setClip(cx, cy, cw, ch);
	}

	/**
	 *
	 *  Key handling 
	 *
	 */
	/**

	protected boolean handleKey(int key) {
		if (key == KEY_UP) {
			this.prevHyperlink();
		} else if (key == KEY_DOWN) {
			this.nextHyperlink(true);
		} else if (key == KEY_LEFT) {
			this.pageUp();
		} else if (key == KEY_RIGHT) {
			this.pageDown();
		} else {
			return false;
		}
		invalidate();
		canvas().doCommand(this, cmPosChanged);
		
		return true;
		
	}
*/
	public String getHyperlink() {
		if (selectedLinkId < 0) return null;
		HtmlItem item = findLinkItem(selectedLinkId);
//		System.out.println(new String (((HtmlTextItem)item).link));
		if (itemIsVisible(item)) {
			if (item.link == null)
				return (((HtmlTextItem)item).text);
			else
				return item.link;
		} else {
			return null;
		}
	}

	protected void doHyperlink(byte [] hyperlink) {
	}

	private HtmlItem findLinkItem(int id) {
		for(int i = 0; i < items.size(); i++) {
			HtmlItem item = (HtmlItem) items.elementAt(i);
			if (item.linkId == id) {
				return item;
			}
		}
		return null;
	}

	private boolean itemIsVisible(HtmlItem item) {
		return (item.ypos >= currentPosY
				&& (item.ypos + item.height) <= (currentPosY + height));
	}

	public void pageUp () {
		setCurrentPosY(currentPosY - pageSize);
		for(int i = 0; i < items.size(); i++) {
			HtmlItem item = (HtmlItem) items.elementAt(i);
			if (item.linkId != -1 && itemIsVisible(item)) {
				selectedLinkId =item.linkId;
				break;
			}
		}
	}

	public void pageDown () {
		setCurrentPosY(currentPosY + pageSize);
		for(int i = items.size() - 1; i >= 0; i--) {
			HtmlItem item = (HtmlItem) items.elementAt(i);
			if (item.linkId != -1 && itemIsVisible(item)) {
				selectedLinkId =item.linkId;
				break;
			}
		}
	}

	private void setCurrentPosY(int y) {
		if (y + height > maxHeight) y = maxHeight - height;
		if (y < 0) y = 0;
		currentPosY = y;
	}

	/**
	 * Advance selected hyperlink to next Link. If it's not on this
	 * page, scroll display by one line. If we scrolled, reselect to
	 * find select first lsLink on the page if there is one visible.
	 */
	 public void nextHyperlink (boolean autoScroll) {
		 HtmlItem nextItem;
		 boolean scrolldown = true;
		 if (selectedLinkId < maxLinkId) {
			 nextItem = findLinkItem(selectedLinkId + 1);
			 if (nextItem != null && itemIsVisible(nextItem)) {
				 selectedLinkId++;
				 scrolldown = false;

			 }
		 }

		 if (autoScroll && scrolldown) {
			 setCurrentPosY(currentPosY + lineStep);
		 }
	 }

	 /**
	  * Backup to select the previous hyperlink HtmlItem, or go to prev
	  * page if no links on this page.  (Need to update this to work
	  * like nextHyperlink does now) 
	  */
	 public void prevHyperlink () {
		 HtmlItem nextItem;
		 boolean scroll = true;
		 if (selectedLinkId > 0) {
			 nextItem = findLinkItem(selectedLinkId - 1);
			 if (nextItem != null && itemIsVisible(nextItem)) {
				 selectedLinkId--;
				 scroll = false;

			 }
		 }

		 if (scroll) {
			 setCurrentPosY(currentPosY - lineStep);
		 }
	 }


	 /**
	  * Get the hyperlink target of an anchor. This should
	  * be changed to take the item directly, instead of 
	  * maxLinkId int.
	  */
	 String getLinkTarget (int linkId) {
		 for(int i = 0; i < items.size();i++) {
			 HtmlItem item = (HtmlItem) items.elementAt(i);
			 if (item.linkId == linkId) {
				 return item.link;
			 }

		 }
		 return null;
	 }

	 /** 
	  * @return  the currently selected item, or null if no item is selected
	  */
	 HtmlItem getSelectedItem () {
		 HtmlItem found = null;
		 for (int i = 0; i < items.size(); i++) {
			 HtmlItem item = (HtmlItem) items.elementAt(i);	
			 if (item.linkId == selectedLinkId) {
				 found = item;
			 }

		 }
		 return found;
	 }

}

/**
 *
 * Html Items classes
 *
 *
 *
 */
abstract class HtmlItem {

	public int xpos, ypos, width, height;
	public int linkId = -1;
	public String link = null;

	public HtmlItem () {
	}

	abstract void paint(Graphics g);
	public void paintSelected(Graphics g) {
		paint(g);
	}
}

class HtmlBulletItem extends HtmlItem {
	public HtmlBulletItem() {
		super();
		width = 8;
		height = 4;
	}

	public void paint(Graphics g) {
		g.setColor(HtmlHRulerItem.color);
		g.fillRect(xpos, ypos, 4, height);
	}
}


class HtmlTextItem extends HtmlItem {
	String text;
	Font font;
	int  color;
	static public int colorBckHighlight = 0x00bd794a;
    static public int colorTextHighlight = 0x00ffffff;
	static public int cornerAngle = 5;
	/**
	 * Creates a new instance of HtmlTextItem
	 */
	public HtmlTextItem(String t, Font f, int c) {
		super();
		font = f;
		color = c;
		text = t;
		width = f.stringWidth(text);
		height = f.getHeight();
	}

	public void paintSelected(Graphics g) {
		g.setFont(font);
		g.setColor(colorBckHighlight);
		g.fillRoundRect(xpos, ypos, width, height, cornerAngle, cornerAngle);
		g.setColor(colorTextHighlight);
		g.drawString(text, xpos, ypos, 0);
	}

	public void paint(Graphics g) {
		g.setColor(color);
		g.setFont(font);
		g.drawString(text, xpos, ypos, 0);
	}
}

class HtmlHRulerItem extends HtmlItem {
	public static int lineWidth = 1;
	public static int color = 0x00808080;
	/**
	 * Creates a new instance of HtmlHRulerItem
	 */
	public HtmlHRulerItem(int pageWidth) {
		super();
		height = lineWidth + 6;
		width = pageWidth;
		xpos = 0;
	}

	public void paint(Graphics g) {
		g.setColor(color);
		g.fillRect(xpos, ypos + ((height - lineWidth) / 2), width, lineWidth);
	}

}


class HtmlImageItem extends HtmlItem {
	public Image image;

	static private Vector imageCache = new Vector();
	static private Vector urlImageCache  = new Vector();

	static public Image findImageInCache(Bytes url) {
		String cUrl;
		for (int i = 0; i < urlImageCache.size(); i++) {
			cUrl = (String) urlImageCache.elementAt(i);
			if (url.equals(cUrl)) {
				return (Image) imageCache.elementAt(i);
			}
		}
		return null;
	}

	static public void addImageToCache(Bytes url, Image i) {
		imageCache.addElement(i);
		urlImageCache.addElement(url.toString());
	}

	/**
	 * Creates a new instance of HtmlImageItem
	 */
	 public HtmlImageItem(Bytes imageUrl) {
		super();
		image = findImageInCache(imageUrl);
		if (image == null) {
			try {
				image = Image.createImage(imageUrl.toString());
				addImageToCache(imageUrl, image);
			} catch (Exception e) {}
		}
		if (image != null) {
			width = image.getWidth();
			height = image.getHeight();
		} else {
			width = 20;
			height = 20;
		}
	 }

	 public void paintSelected(Graphics g) {
		 g.setColor(HtmlTextItem.colorBckHighlight);
		 g.drawRect(xpos - 1, ypos - 1, width + 1, height + 1);
		 g.drawImage(image, xpos, ypos, 0);
	 }

	 public void paint(Graphics g) {
		 g.drawImage(image, xpos, ypos, 0);
	 }
}
