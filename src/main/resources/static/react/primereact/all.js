var lastId = 0;
function UniqueComponentId(prefix = 'pr_id_') {
    lastId++;
    return `${prefix}${lastId}`;
}

class DomHandler {

    static innerWidth(el) {
        if (el) {
            let width = el.offsetWidth;
            let style = getComputedStyle(el);

            width += parseFloat(style.paddingLeft) + parseFloat(style.paddingRight);
            return width;
        }
        return 0;
    }

    static width(el) {
        if (el) {
            let width = el.offsetWidth;
            let style = getComputedStyle(el);

            width -= parseFloat(style.paddingLeft) + parseFloat(style.paddingRight);
            return width;
        }
        return 0;
    }

    static getWindowScrollTop() {
        let doc = document.documentElement;
        return (window.pageYOffset || doc.scrollTop) - (doc.clientTop || 0);
    }

    static getWindowScrollLeft() {
        let doc = document.documentElement;
        return (window.pageXOffset || doc.scrollLeft) - (doc.clientLeft || 0);
    }

    static getOuterWidth(el, margin) {
        if (el) {
            let width = el.offsetWidth;

            if (margin) {
                let style = getComputedStyle(el);
                width += parseFloat(style.marginLeft) + parseFloat(style.marginRight);
            }

            return width;
        }
        return 0;
    }

    static getOuterHeight(el, margin) {
        if (el) {
            let height = el.offsetHeight;

            if (margin) {
                let style = getComputedStyle(el);
                height += parseFloat(style.marginTop) + parseFloat(style.marginBottom);
            }

            return height;
        }
        return 0;
    }

	static getClientHeight(el, margin) {
        if (el) {
            let height = el.clientHeight;

            if (margin) {
                let style = getComputedStyle(el);
                height += parseFloat(style.marginTop) + parseFloat(style.marginBottom);
            }

            return height;
        }
        return 0;
    }

    static getViewport() {
        let win = window,
            d = document,
            e = d.documentElement,
            g = d.getElementsByTagName('body')[0],
            w = win.innerWidth || e.clientWidth || g.clientWidth,
            h = win.innerHeight || e.clientHeight || g.clientHeight;

        return {width: w, height: h};
    }

    static getOffset(el) {
        if (el) {
            let rect = el.getBoundingClientRect();

            return {
                top: rect.top + document.body.scrollTop,
                left: rect.left + document.body.scrollLeft
            };
        }
        return {
            top: 'auto',
            left: 'auto'
        };
    }

    static generateZIndex() {
        this.zindex = this.zindex||999;
        return ++this.zindex;
    }

    static getCurrentZIndex() {
        return this.zindex;
    }

    static index(element) {
        if (element) {
            let children = element.parentNode.childNodes;
            let num = 0;
            for (var i = 0; i < children.length; i++) {
                if (children[i] === element) return num;
                if (children[i].nodeType === 1) num++;
            }
        }
        return -1;
    }

    static addMultipleClasses(element, className) {
        if (element) {
            if (element.classList) {
                let styles = className.split(' ');
                for (let i = 0; i < styles.length; i++) {
                    element.classList.add(styles[i]);
                }

            }
            else {
                let styles = className.split(' ');
                for (let i = 0; i < styles.length; i++) {
                    element.className += ' ' + styles[i];
                }
            }
        }
    }

    static addClass(element, className) {
        if (element) {
            if (element.classList)
                element.classList.add(className);
            else
                element.className += ' ' + className;
        }
    }

    static removeClass(element, className) {
        if (element) {
            if (element.classList)
                element.classList.remove(className);
            else
                element.className = element.className.replace(new RegExp('(^|\\b)' + className.split(' ').join('|') + '(\\b|$)', 'gi'), ' ');
        }
    }

    static hasClass(element, className) {
        if (element) {
            if (element.classList)
                return element.classList.contains(className);
            else
                return new RegExp('(^| )' + className + '( |$)', 'gi').test(element.className);
        }
    }

    static find(element, selector) {
        return element ? Array.from(element.querySelectorAll(selector)) : [];
    }

    static findSingle(element, selector) {
        if (element) {
            return element.querySelector(selector);
        }
        return null;
    }

    static getHeight(el) {
        if (el) {
            let height = el.offsetHeight;
            let style = getComputedStyle(el);

            height -= parseFloat(style.paddingTop) + parseFloat(style.paddingBottom) + parseFloat(style.borderTopWidth) + parseFloat(style.borderBottomWidth);

            return height;
        }
        return 0;
    }

    static getWidth(el) {
        if (el) {
            let width = el.offsetWidth;
            let style = getComputedStyle(el);

            width -= parseFloat(style.paddingLeft) + parseFloat(style.paddingRight) + parseFloat(style.borderLeftWidth) + parseFloat(style.borderRightWidth);

            return width;
        }
        return 0;
    }

    static absolutePosition(element, target) {
        if (element) {
            let elementDimensions = element.offsetParent ? { width: element.offsetWidth, height: element.offsetHeight } : this.getHiddenElementDimensions(element)
            let elementOuterHeight = elementDimensions.height;
            let elementOuterWidth = elementDimensions.width;
            let targetOuterHeight = target.offsetHeight;
            let targetOuterWidth = target.offsetWidth;
            let targetOffset = target.getBoundingClientRect();
            let windowScrollTop = this.getWindowScrollTop();
            let windowScrollLeft = this.getWindowScrollLeft();
            let viewport = this.getViewport();
            let top, left;

            if (targetOffset.top + targetOuterHeight + elementOuterHeight > viewport.height) {
                top = targetOffset.top + windowScrollTop - elementOuterHeight;
                if(top < 0) {
                    top = windowScrollTop;
                }
            }
            else {
                top = targetOuterHeight + targetOffset.top + windowScrollTop;
            }

            if (targetOffset.left + targetOuterWidth + elementOuterWidth > viewport.width)
                left = Math.max(0, targetOffset.left + windowScrollLeft + targetOuterWidth - elementOuterWidth);
            else
                left = targetOffset.left + windowScrollLeft;

            element.style.top = top + 'px';
            element.style.left = left + 'px';
        }
    }

    static relativePosition(element, target) {
        if (element) {
            let elementDimensions = element.offsetParent ? { width: element.offsetWidth, height: element.offsetHeight } : this.getHiddenElementDimensions(element);
            const targetHeight = target.offsetHeight;
            const targetOffset = target.getBoundingClientRect();
            const viewport = this.getViewport();
            let top, left;

            if ((targetOffset.top + targetHeight + elementDimensions.height) > viewport.height) {
                top = -1 * (elementDimensions.height);
                if (targetOffset.top + top < 0) {
                    top = -1 * targetOffset.top;
                }
            }
            else {
                top = targetHeight;
            }

            if (elementDimensions.width > viewport.width) {
                // element wider then viewport and cannot fit on screen (align at left side of viewport)
                left = targetOffset.left * -1;
            }
            else if ((targetOffset.left + elementDimensions.width) > viewport.width) {
                // element wider then viewport but can be fit on screen (align at right side of viewport)
                left = (targetOffset.left + elementDimensions.width - viewport.width) * -1;
            }
            else {
                // element fits on screen (align with target)
                left = 0;
            }

            element.style.top = top + 'px';
            element.style.left = left + 'px';
        }
    }

    static getHiddenElementOuterHeight(element) {
        if (element) {
            element.style.visibility = 'hidden';
            element.style.display = 'block';
            let elementHeight = element.offsetHeight;
            element.style.display = 'none';
            element.style.visibility = 'visible';

            return elementHeight;
        }
        return 0;
    }

    static getHiddenElementOuterWidth(element) {
        if (element) {
            element.style.visibility = 'hidden';
            element.style.display = 'block';
            let elementWidth = element.offsetWidth;
            element.style.display = 'none';
            element.style.visibility = 'visible';

            return elementWidth;
        }
        return 0;
    }

    static getHiddenElementDimensions(element) {
        let dimensions = {};
        if (element) {
            element.style.visibility = 'hidden';
            element.style.display = 'block';
            dimensions.width = element.offsetWidth;
            dimensions.height = element.offsetHeight;
            element.style.display = 'none';
            element.style.visibility = 'visible';
        }
        return dimensions;
    }

    static fadeIn(element, duration) {
        if (element) {
            element.style.opacity = 0;

            let last = +new Date();
            let opacity = 0;
            let tick = function () {
                opacity = +element.style.opacity + (new Date().getTime() - last) / duration;
                element.style.opacity = opacity;
                last = +new Date();

                if (+opacity < 1) {
                    (window.requestAnimationFrame && requestAnimationFrame(tick)) || setTimeout(tick, 16);
                }
            };

            tick();
        }
    }

    static fadeOut(element, ms) {
        if (element) {
            let opacity = 1,
                interval = 50,
                duration = ms,
                gap = interval / duration;

            let fading = setInterval(() => {
                opacity -= gap;

                if (opacity <= 0) {
                    opacity = 0;
                    clearInterval(fading);
                }

                element.style.opacity = opacity;
            }, interval);
        }
    }

    static getUserAgent() {
        return navigator.userAgent;
    }

    static isIOS() {
        return /iPad|iPhone|iPod/.test(navigator.userAgent) && !window['MSStream'];
    }

    static isAndroid() {
        return /(android)/i.test(navigator.userAgent);
    }

    static appendChild(element, target) {
        if(this.isElement(target))
            target.appendChild(element);
        else if(target.el && target.el.nativeElement)
            target.el.nativeElement.appendChild(element);
        else
            throw new Error('Cannot append ' + target + ' to ' + element);
    }

    static scrollInView(container, item) {
        let borderTopValue = getComputedStyle(container).getPropertyValue('borderTopWidth');
        let borderTop = borderTopValue ? parseFloat(borderTopValue) : 0;
        let paddingTopValue = getComputedStyle(container).getPropertyValue('paddingTop');
        let paddingTop = paddingTopValue ? parseFloat(paddingTopValue) : 0;
        let containerRect = container.getBoundingClientRect();
        let itemRect = item.getBoundingClientRect();
        let offset = (itemRect.top + document.body.scrollTop) - (containerRect.top + document.body.scrollTop) - borderTop - paddingTop;
        let scroll = container.scrollTop;
        let elementHeight = container.clientHeight;
        let itemHeight = this.getOuterHeight(item);

        if (offset < 0) {
            container.scrollTop = scroll + offset;
        }
        else if ((offset + itemHeight) > elementHeight) {
            container.scrollTop = scroll + offset - elementHeight + itemHeight;
        }
    }

    static clearSelection() {
        if(window.getSelection) {
            if(window.getSelection().empty) {
                window.getSelection().empty();
            } else if(window.getSelection().removeAllRanges && window.getSelection().rangeCount > 0 && window.getSelection().getRangeAt(0).getClientRects().length > 0) {
                window.getSelection().removeAllRanges();
            }
        }
        else if(document['selection'] && document['selection'].empty) {
            try {
                document['selection'].empty();
            } catch(error) {
                //ignore IE bug
            }
        }
    }

    static calculateScrollbarWidth(el) {
        if (el) {
            let style = getComputedStyle(el);
            return (el.offsetWidth - el.clientWidth - parseFloat(style.borderLeftWidth) - parseFloat(style.borderRightWidth));
        }
        else {
            if(this.calculatedScrollbarWidth != null)
                return this.calculatedScrollbarWidth;

            let scrollDiv = document.createElement("div");
            scrollDiv.className = "p-scrollbar-measure";
            document.body.appendChild(scrollDiv);

            let scrollbarWidth = scrollDiv.offsetWidth - scrollDiv.clientWidth;
            document.body.removeChild(scrollDiv);

            this.calculatedScrollbarWidth = scrollbarWidth;

            return scrollbarWidth;
        }
    }

    static getBrowser() {
        if(!this.browser) {
            let matched = this.resolveUserAgent();
            this.browser = {};

            if (matched.browser) {
                this.browser[matched.browser] = true;
                this.browser['version'] = matched.version;
            }

            if (this.browser['chrome']) {
                this.browser['webkit'] = true;
            } else if (this.browser['webkit']) {
                this.browser['safari'] = true;
            }
        }

        return this.browser;
    }

    static resolveUserAgent() {
        let ua = navigator.userAgent.toLowerCase();
        let match = /(chrome)[ ]([\w.]+)/.exec(ua) ||
            /(webkit)[ ]([\w.]+)/.exec(ua) ||
            /(opera)(?:.*version|)[ ]([\w.]+)/.exec(ua) ||
            /(msie) ([\w.]+)/.exec(ua) ||
            (ua.indexOf("compatible") < 0 && /(mozilla)(?:.*? rv:([\w.]+)|)/.exec(ua)) ||
            [];

        return {
            browser: match[1] || "",
            version: match[2] || "0"
        };
    }

    static isVisible(element) {
        return element && element.offsetParent != null;
    }

    static getFocusableElements(element) {
        let focusableElements = DomHandler.find(element, `button:not([tabindex = "-1"]):not([disabled]):not([style*="display:none"]):not([hidden]), 
                [href][clientHeight][clientWidth]:not([tabindex = "-1"]):not([disabled]):not([style*="display:none"]):not([hidden]), 
                input:not([tabindex = "-1"]):not([disabled]):not([style*="display:none"]):not([hidden]), select:not([tabindex = "-1"]):not([disabled]):not([style*="display:none"]):not([hidden]), 
                textarea:not([tabindex = "-1"]):not([disabled]):not([style*="display:none"]):not([hidden]), [tabIndex]:not([tabIndex = "-1"]):not([disabled]):not([style*="display:none"]):not([hidden]), 
                [contenteditable]:not([tabIndex = "-1"]):not([disabled]):not([style*="display:none"]):not([hidden])`
        );

        let visibleFocusableElements = [];
        for (let focusableElement of focusableElements) {
            if (getComputedStyle(focusableElement).display !== "none" && getComputedStyle(focusableElement).visibility !== "hidden")
                visibleFocusableElements.push(focusableElement);
        }

        return visibleFocusableElements;
    }
}

class ObjectUtils {

    static equals(obj1, obj2, field) {
        if(field && obj1 && typeof obj1 === 'object' && obj2 && typeof obj2 === 'object')
            return (this.resolveFieldData(obj1, field) === this.resolveFieldData(obj2, field));
        else
            return this.deepEquals(obj1, obj2);
    }

    static deepEquals(a, b) {
        if (a === b) return true;

        if (a && b && typeof a == 'object' && typeof b == 'object') {
            var arrA = Array.isArray(a)
                , arrB = Array.isArray(b)
                , i
                , length
                , key;

            if (arrA && arrB) {
                length = a.length;
                if (length !== b.length) return false;
                for (i = length; i-- !== 0;)
                    if (!this.deepEquals(a[i], b[i])) return false;
                return true;
            }

            if (arrA !== arrB) return false;

            var dateA = a instanceof Date
                , dateB = b instanceof Date;
            if (dateA !== dateB) return false;
            if (dateA && dateB) return a.getTime() === b.getTime();

            var regexpA = a instanceof RegExp
                , regexpB = b instanceof RegExp;
            if (regexpA !== regexpB) return false;
            if (regexpA && regexpB) return a.toString() === b.toString();

            var keys = Object.keys(a);
            length = keys.length;

            if (length !== Object.keys(b).length)
                return false;

            for (i = length; i-- !== 0;)
                if (!Object.prototype.hasOwnProperty.call(b, keys[i])) return false;

            for (i = length; i-- !== 0;) {
                key = keys[i];
                if (!this.deepEquals(a[key], b[key])) return false;
            }

            return true;
        }

        /*eslint no-self-compare: "off"*/
        return a !== a && b !== b;
    }

    static resolveFieldData(data, field) {
        if(data && field) {
            if (this.isFunction(field)) {
                return field(data);
            }
            else if(field.indexOf('.') === -1) {
                return data[field];
            }
            else {
                let fields = field.split('.');
                let value = data;
                for(var i = 0, len = fields.length; i < len; ++i) {
                    if (value == null) {
                        return null;
                    }
                    value = value[fields[i]];
                }
                return value;
            }
        }
        else {
            return null;
        }
    }

    static isFunction(obj) {
        return !!(obj && obj.constructor && obj.call && obj.apply);
    }

    static findDiffKeys(obj1, obj2) {
        if (!obj1 || !obj2) {
            return {};
        }

        return Object.keys(obj1).filter(key => !obj2.hasOwnProperty(key)).reduce((result, current) => {
            result[current] = obj1[current];
            return result;
        }, {});
    }

    static reorderArray(value, from, to) {
        let target;
        if(value && (from !== to)) {
            if(to >= value.length) {
                target = to - value.length;
                while((target--) + 1) {
                    value.push(undefined);
                }
            }
            value.splice(to, 0, value.splice(from, 1)[0]);
        }
    }

    static findIndexInList(value, list) {
        let index = -1;

        if(list) {
            for(let i = 0; i < list.length; i++) {
                if(list[i] === value) {
                    index = i;
                    break;
                }
            }
        }

        return index;
    }

    static getJSXElement(obj, ...params) {
        return this.isFunction(obj) ? obj(...params) : obj;
    }

    static removeAccents(str) {
        if (str && str.search(/[\xC0-\xFF]/g) > -1) {
            str = str
                    .replace(/[\xC0-\xC5]/g, "A")
                    .replace(/[\xC6]/g, "AE")
                    .replace(/[\xC7]/g, "C")
                    .replace(/[\xC8-\xCB]/g, "E")
                    .replace(/[\xCC-\xCF]/g, "I")
                    .replace(/[\xD0]/g, "D")
                    .replace(/[\xD1]/g, "N")
                    .replace(/[\xD2-\xD6\xD8]/g, "O")
                    .replace(/[\xD9-\xDC]/g, "U")
                    .replace(/[\xDD]/g, "Y")
                    .replace(/[\xDE]/g, "P")
                    .replace(/[\xE0-\xE5]/g, "a")
                    .replace(/[\xE6]/g, "ae")
                    .replace(/[\xE7]/g, "c")
                    .replace(/[\xE8-\xEB]/g, "e")
                    .replace(/[\xEC-\xEF]/g, "i")
                    .replace(/[\xF1]/g, "n")
                    .replace(/[\xF2-\xF6\xF8]/g, "o")
                    .replace(/[\xF9-\xFC]/g, "u")
                    .replace(/[\xFE]/g, "p")
                    .replace(/[\xFD\xFF]/g, "y");
        }

        return str;
    }
}

class FilterUtils {

    static filter(value, fields, filterValue, filterMatchMode, filterLocale) {
        let filteredItems  = [];
        let filterText = ObjectUtils.removeAccents(filterValue).toLocaleLowerCase(filterLocale);

        if (value) {
            for (let item of value) {
                for (let field of fields) {
                    let fieldValue = ObjectUtils.removeAccents(String(ObjectUtils.resolveFieldData(item, field))).toLocaleLowerCase(filterLocale);

                    if (FilterUtils[filterMatchMode](fieldValue,filterText, filterLocale)) {
                        filteredItems.push(item);
                        break;
                    }
                }
            }
        }

        return filteredItems;
    }

    static startsWith(value, filter, filterLocale) {
        if (filter === undefined || filter === null || filter.trim() === '') {
            return true;
        }

        if (value === undefined || value === null) {
            return false;
        }

        let filterValue = ObjectUtils.removeAccents(filter.toString()).toLocaleLowerCase(filterLocale);
        let stringValue = ObjectUtils.removeAccents(value.toString()).toLocaleLowerCase(filterLocale);

        return stringValue.slice(0, filterValue.length) === filterValue;
    }

    static contains(value, filter, filterLocale) {
        if (filter === undefined || filter === null || (typeof filter === 'string' && filter.trim() === '')) {
            return true;
        }

        if (value === undefined || value === null) {
            return false;
        }

        let filterValue = ObjectUtils.removeAccents(filter.toString()).toLocaleLowerCase(filterLocale);
        let stringValue = ObjectUtils.removeAccents(value.toString()).toLocaleLowerCase(filterLocale);

        return stringValue.indexOf(filterValue) !== -1;
    }

    static endsWith(value, filter, filterLocale) {
        if (filter === undefined || filter === null || filter.trim() === '') {
            return true;
        }

        if (value === undefined || value === null) {
            return false;
        }

        let filterValue = ObjectUtils.removeAccents(filter.toString()).toLocaleLowerCase(filterLocale);
        let stringValue = ObjectUtils.removeAccents(value.toString()).toLocaleLowerCase(filterLocale);

        return stringValue.indexOf(filterValue, stringValue.length - filterValue.length) !== -1;
    }

    static equals(value, filter, filterLocale) {
        if (filter === undefined || filter === null || (typeof filter === 'string' && filter.trim() === '')) {
            return true;
        }

        if (value === undefined || value === null) {
            return false;
        }

        if (value.getTime && filter.getTime)
            return value.getTime() === filter.getTime();
        else
            return ObjectUtils.removeAccents(value.toString()).toLocaleLowerCase(filterLocale) === ObjectUtils.removeAccents(filter.toString()).toLocaleLowerCase(filterLocale);
    }

    static notEquals(value, filter, filterLocale) {
        if (filter === undefined || filter === null || (typeof filter === 'string' && filter.trim() === '')) {
            return false;
        }

        if (value === undefined || value === null) {
            return true;
        }

        if (value.getTime && filter.getTime)
            return value.getTime() !== filter.getTime();
        else
            return ObjectUtils.removeAccents(value.toString()).toLocaleLowerCase(filterLocale) !== ObjectUtils.removeAccents(filter.toString()).toLocaleLowerCase(filterLocale);
    }

    static in(value, filter, filterLocale) {
        if (filter === undefined || filter === null || filter.length === 0) {
            return true;
        }

        if (value === undefined || value === null) {
            return false;
        }

        for (let i = 0; i < filter.length; i++) {
            if (ObjectUtils.equals(value, filter[i])) {
                return true;
            }
        }

        return false;
    }

    static lt(value, filter, filterLocale) {
        if (filter === undefined || filter === null || (filter.trim && filter.trim().length === 0)) {
            return true;
        }

        if (value === undefined || value === null) {
            return false;
        }

        if (value.getTime && filter.getTime)
            return value.getTime() < filter.getTime();
        else
            return value < parseFloat(filter);
    }

    static lte(value, filter, filterLocale) {
        if (filter === undefined || filter === null || (filter.trim && filter.trim().length === 0)) {
            return true;
        }

        if (value === undefined || value === null) {
            return false;
        }

        if (value.getTime && filter.getTime)
            return value.getTime() <= filter.getTime();
        else
            return value <= parseFloat(filter);
    }

    static gt(value, filter, filterLocale) {
        if (filter === undefined || filter === null || (filter.trim && filter.trim().length === 0)) {
            return true;
        }

        if (value === undefined || value === null) {
            return false;
        }

        if (value.getTime && filter.getTime)
            return value.getTime() > filter.getTime();
        else
            return value > parseFloat(filter);
    }

    static gte(value, filter, filterLocale) {
        if (filter === undefined || filter === null || (filter.trim && filter.trim().length === 0)) {
            return true;
        }

        if (value === undefined || value === null) {
            return false;
        }

        if (value.getTime && filter.getTime)
            return value.getTime() >= filter.getTime();
        else
            return value >= parseFloat(filter);
    }

}


class Tooltip {
  constructor(props) {
    this.target = props.target;
    this.targetContainer = props.targetContainer;
    this.content = props.content;
    this.options = props.options || {};
    this.options.event = this.options.event || 'hover';
    this.options.position = this.options.position || 'right';
    this.bindEvents();
  }

  bindEvents() {
    if (this.options.event === 'hover') {
      this.mouseEnterListener = this.onMouseEnter.bind(this);
      this.mouseLeaveListener = this.onMouseLeave.bind(this);
      this.clickListener = this.onClick.bind(this);
      this.target.addEventListener('mouseenter', this.mouseEnterListener);
      this.target.addEventListener('mouseleave', this.mouseLeaveListener);
      this.target.addEventListener('click', this.clickListener);
    } else if (this.options.event === 'focus') {
      this.focusListener = this.onFocus.bind(this);
      this.blurListener = this.onBlur.bind(this);
      this.target.addEventListener('focus', this.focusListener);
      this.target.addEventListener('blur', this.blurListener);
    }
  }

  unbindEvents() {
    if (this.options.event === 'hover') {
      this.target.removeEventListener('mouseenter', this.mouseEnterListener);
      this.target.removeEventListener('mouseleave', this.mouseLeaveListener);
      this.target.removeEventListener('click', this.clickListener);
    } else if (this.options.event === 'focus') {
      this.target.removeEventListener('focus', this.focusListener);
      this.target.removeEventListener('blur', this.blurListener);
    }

    this.unbindDocumentResizeListener();
  }

  onMouseEnter() {
    if (!this.container && !this.showTimeout) {
      this.activate();
    }
  }

  onMouseLeave() {
    this.deactivate();
  }

  onFocus() {
    this.activate();
  }

  onBlur() {
    this.deactivate();
  }

  onClick() {
    this.deactivate();
  }

  activate() {
    this.clearHideTimeout();
    if (this.options.showDelay) this.showTimeout = setTimeout(() => {
      this.show();
    }, this.options.showDelay);else this.show();
  }

  deactivate() {
    this.clearShowTimeout();
    if (this.options.hideDelay) this.hideTimeout = setTimeout(() => {
      this.hide();
    }, this.options.hideDelay);else this.hide();
  }

  clearShowTimeout() {
    if (this.showTimeout) {
      clearTimeout(this.showTimeout);
      this.showTimeout = null;
    }
  }

  clearHideTimeout() {
    if (this.hideTimeout) {
      clearTimeout(this.hideTimeout);
      this.hideTimeout = null;
    }
  }

  clearTimeouts() {
    this.clearShowTimeout();
    this.clearHideTimeout();
  }

  updateContent(content) {
    this.content = content;
  }

  show() {
    if (!this.content) {
      return;
    }

    this.create();
    this.align();
    DomHandler.fadeIn(this.container, 250);
    this.container.style.zIndex = ++DomHandler.zindex;
    this.bindDocumentResizeListener();
  }

  hide() {
    this.remove();
  }

  create() {
    this.container = document.createElement('div');
    let tooltipArrow = document.createElement('div');
    tooltipArrow.className = 'p-tooltip-arrow';
    this.container.appendChild(tooltipArrow);
    this.tooltipText = document.createElement('div');
    this.tooltipText.className = 'p-tooltip-text'; //todo: JSX support

    this.tooltipText.innerHTML = this.content;
    this.container.appendChild(this.tooltipText);
    document.body.appendChild(this.container);
    this.container.style.display = 'inline-block';
  }

  remove() {
    if (this.container && this.container.parentElement) {
      document.body.removeChild(this.container);
    }

    this.unbindDocumentResizeListener();
    this.clearTimeouts();
    this.container = null;
  }

  align() {
    switch (this.options.position) {
      case 'top':
        this.alignTop();

        if (this.isOutOfBounds()) {
          this.alignBottom();
        }

        break;

      case 'bottom':
        this.alignBottom();

        if (this.isOutOfBounds()) {
          this.alignTop();
        }

        break;

      case 'left':
        this.alignLeft();

        if (this.isOutOfBounds()) {
          this.alignRight();

          if (this.isOutOfBounds()) {
            this.alignTop();

            if (this.isOutOfBounds()) {
              this.alignBottom();
            }
          }
        }

        break;

      case 'right':
        this.alignRight();

        if (this.isOutOfBounds()) {
          this.alignLeft();

          if (this.isOutOfBounds()) {
            this.alignTop();

            if (this.isOutOfBounds()) {
              this.alignBottom();
            }
          }
        }

        break;

      default:
        throw new Error('Invalid position:' + this.options.position);
    }
  }

  getHostOffset() {
    let target = this.targetContainer || this.target;
    let offset = target.getBoundingClientRect();
    let targetLeft = offset.left + DomHandler.getWindowScrollLeft();
    let targetTop = offset.top + DomHandler.getWindowScrollTop();
    return {
      left: targetLeft,
      top: targetTop
    };
  }

  alignRight() {
    this.preAlign('right');
    let target = this.targetContainer || this.target;
    let hostOffset = this.getHostOffset();
    let left = hostOffset.left + DomHandler.getOuterWidth(target);
    let top = hostOffset.top + (DomHandler.getOuterHeight(target) - DomHandler.getOuterHeight(this.container)) / 2;
    this.container.style.left = left + 'px';
    this.container.style.top = top + 'px';
  }

  alignLeft() {
    this.preAlign('left');
    let target = this.targetContainer || this.target;
    let hostOffset = this.getHostOffset();
    let left = hostOffset.left - DomHandler.getOuterWidth(this.container);
    let top = hostOffset.top + (DomHandler.getOuterHeight(target) - DomHandler.getOuterHeight(this.container)) / 2;
    this.container.style.left = left + 'px';
    this.container.style.top = top + 'px';
  }

  alignTop() {
    this.preAlign('top');
    let target = this.targetContainer || this.target;
    let hostOffset = this.getHostOffset();
    let left = hostOffset.left + (DomHandler.getOuterWidth(target) - DomHandler.getOuterWidth(this.container)) / 2;
    let top = hostOffset.top - DomHandler.getOuterHeight(this.container);
    this.container.style.left = left + 'px';
    this.container.style.top = top + 'px';
  }

  alignBottom() {
    this.preAlign('bottom');
    let target = this.targetContainer || this.target;
    let hostOffset = this.getHostOffset();
    let left = hostOffset.left + (DomHandler.getOuterWidth(target) - DomHandler.getOuterWidth(this.container)) / 2;
    let top = hostOffset.top + DomHandler.getOuterHeight(target);
    this.container.style.left = left + 'px';
    this.container.style.top = top + 'px';
  }

  preAlign(position) {
    this.container.style.left = -999 + 'px';
    this.container.style.top = -999 + 'px';
    let defaultClassName = 'p-tooltip p-component p-tooltip-' + position;
    this.container.className = this.options.className ? defaultClassName + ' ' + this.options.className : defaultClassName;
  }

  isOutOfBounds() {
    let offset = this.container.getBoundingClientRect();
    let targetTop = offset.top;
    let targetLeft = offset.left;
    let width = DomHandler.getOuterWidth(this.container);
    let height = DomHandler.getOuterHeight(this.container);
    let viewport = DomHandler.getViewport();
    return targetLeft + width > viewport.width || targetLeft < 0 || targetTop < 0 || targetTop + height > viewport.height;
  }

  bindDocumentResizeListener() {
    this.resizeListener = this.onWindowResize.bind(this);
    window.addEventListener('resize', this.resizeListener);
  }

  unbindDocumentResizeListener() {
    if (this.resizeListener) {
      window.removeEventListener('resize', this.resizeListener);
      this.resizeListener = null;
    }
  }

  onWindowResize() {
    this.hide();
  }

  destroy() {
    this.unbindEvents();
    this.remove();
    this.target = null;
    this.targetContainer = null;
  }

}

class Button extends React.Component {
  componentDidMount() {
    if (this.props.tooltip) {
      this.renderTooltip();
    }
  }

  componentDidUpdate(prevProps) {
    if (prevProps.tooltip !== this.props.tooltip) {
      if (this.tooltip) this.tooltip.updateContent(this.props.tooltip);else this.renderTooltip();
    }
  }

  componentWillUnmount() {
    if (this.tooltip) {
      this.tooltip.destroy();
      this.tooltip = null;
    }
  }

  renderTooltip() {
    this.tooltip = new Tooltip({
      target: this.element,
      content: this.props.tooltip,
      options: this.props.tooltipOptions
    });
  }

  renderIcon() {
    if (this.props.icon) {
      let className = classNames(this.props.icon, 'p-c', {
        'p-button-icon-left': this.props.iconPos !== 'right',
        'p-button-icon-right': this.props.iconPos === 'right'
      });
      return _("span", {
        className: className
      });
    } else {
      return null;
    }
  }

  renderLabel() {
    const buttonLabel = this.props.label || 'p-btn';
    return _("span", {
      className: "p-button-text p-c"
    }, buttonLabel);
  }

  render() {
    let className = classNames('p-button p-component', this.props.className, {
      'p-button-icon-only': this.props.icon && !this.props.label,
      'p-button-text-icon-left': this.props.icon && this.props.label && this.props.iconPos === 'left',
      'p-button-text-icon-right': this.props.icon && this.props.label && this.props.iconPos === 'right',
      'p-button-text-only': !this.props.icon && this.props.label,
      'p-disabled': this.props.disabled
    });
    let icon = this.renderIcon();
    let label = this.renderLabel();
    let buttonProps = ObjectUtils.findDiffKeys(this.props, Button.defaultProps);
    return _("button", Object.assign({
      ref: el => this.element = el
    }, buttonProps, {
      className: className
    }), this.props.iconPos === 'left' && icon, label, this.props.iconPos === 'right' && icon, this.props.children);
  }

}

Button.defaultProps = {
	label: null,
	icon: null,
	iconPos: 'left',
	tooltip: null,
	tooltipOptions: null
}

Button.propTypes = {
	label: PropTypes.string,
	icon: PropTypes.string,
	iconPos: PropTypes.string,
	tooltip: PropTypes.string,
	tooltipOptions: PropTypes.object
};


class Calendar extends React.Component {
  constructor(props) {
    super(props);

    if (!this.props.onViewDateChange) {
      let propValue = this.props.value;

      if (Array.isArray(propValue)) {
        propValue = propValue[0];
      }

      let viewDate = this.props.viewDate && this.isValidDate(this.props.viewDate) ? this.props.viewDate : propValue && this.isValidDate(propValue) ? propValue : new Date();
      this.state = {
        viewDate
      };
    }

    this.navigation = null;
    this.onUserInput = this.onUserInput.bind(this);
    this.onInputFocus = this.onInputFocus.bind(this);
    this.onInputBlur = this.onInputBlur.bind(this);
    this.onInputKeyDown = this.onInputKeyDown.bind(this);
    this.onButtonClick = this.onButtonClick.bind(this);
    this.onPrevButtonClick = this.onPrevButtonClick.bind(this);
    this.onNextButtonClick = this.onNextButtonClick.bind(this);
    this.onMonthDropdownChange = this.onMonthDropdownChange.bind(this);
    this.onYearDropdownChange = this.onYearDropdownChange.bind(this);
    this.onTodayButtonClick = this.onTodayButtonClick.bind(this);
    this.onClearButtonClick = this.onClearButtonClick.bind(this);
    this.incrementHour = this.incrementHour.bind(this);
    this.decrementHour = this.decrementHour.bind(this);
    this.incrementMinute = this.incrementMinute.bind(this);
    this.decrementMinute = this.decrementMinute.bind(this);
    this.incrementSecond = this.incrementSecond.bind(this);
    this.decrementSecond = this.decrementSecond.bind(this);
    this.toggleAmPm = this.toggleAmPm.bind(this);
    this.onTimePickerElementMouseDown = this.onTimePickerElementMouseDown.bind(this);
    this.onTimePickerElementMouseUp = this.onTimePickerElementMouseUp.bind(this);
    this.onTimePickerElementMouseLeave = this.onTimePickerElementMouseLeave.bind(this);
  }

  componentDidMount() {
    if (this.props.tooltip) {
      this.renderTooltip();
    }

    if (this.props.inline) {
      this.initFocusableCell();
    }

    if (this.props.value) {
      this.updateInputfield(this.props.value);
    }
  }

  componentDidUpdate(prevProps) {
    if (prevProps.tooltip !== this.props.tooltip) {
      if (this.tooltip) this.tooltip.updateContent(this.props.tooltip);else this.renderTooltip();
    }

    if (!this.props.onViewDateChange && !this.viewStateChanged) {
      let propValue = this.props.value;

      if (Array.isArray(propValue)) {
        propValue = propValue[0];
      }

      let prevPropValue = prevProps.value;

      if (Array.isArray(prevPropValue)) {
        prevPropValue = prevPropValue[0];
      }

      if (!prevPropValue && propValue || propValue && propValue instanceof Date && propValue.getTime() !== prevPropValue.getTime()) {
        let viewDate = this.props.viewDate && this.isValidDate(this.props.viewDate) ? this.props.viewDate : propValue && this.isValidDate(propValue) ? propValue : new Date();
        this.setState({
          viewDate
        }, () => {
          this.viewStateChanged = true;
        });
      }
    }

    if (this.panel) {
      this.updateFocus();
    }

    if (prevProps.value !== this.props.value && (!this.viewStateChanged || !this.panel.offsetParent)) {
      this.updateInputfield(this.props.value);
    }
  }

  componentWillUnmount() {
    if (this.hideTimeout) {
      clearTimeout(this.hideTimeout);
    }

    if (this.mask) {
      this.disableModality();
      this.mask = null;
    }

    if (this.tooltip) {
      this.tooltip.destroy();
      this.tooltip = null;
    }

    this.unbindDocumentClickListener();
    this.unbindDocumentResizeListener();
  }

  renderTooltip() {
    this.tooltip = new Tooltip({
      target: this.inputElement,
      content: this.props.tooltip,
      options: this.props.tooltipOptions
    });
  }

  onInputFocus(event) {
    if (this.props.showOnFocus && !this.panel.offsetParent) {
      this.showOverlay();
    }

    if (this.props.onFocus) {
      this.props.onFocus(event);
    }

    DomHandler.addClass(this.container, 'p-inputwrapper-focus');
  }

  onInputBlur(event) {
    if (this.props.onBlur) {
      this.props.onBlur(event);
    }

    if (!this.props.keepInvalid) {
      this.updateInputfield(this.props.value);
    }

    DomHandler.removeClass(this.container, 'p-inputwrapper-focus');
  }

  onInputKeyDown(event) {
    this.isKeydown = true;

    switch (event.which) {
      //escape
      case 27:
        {
          this.hideOverlay();
          break;
        }
      //tab

      case 9:
        {
          if (this.props.touchUI) {
            this.disableModality();
          }

          if (event.shiftKey) {
            this.hideOverlay();
          }

          break;
        }

      default:
        //no op
        break;
    }
  }

  onUserInput(event) {
    // IE 11 Workaround for input placeholder
    if (!this.isKeydown) {
      return;
    }

    this.isKeydown = false;
    let rawValue = event.target.value;

    try {
      let value = this.parseValueFromString(rawValue);

      if (this.isValidSelection(value)) {
        this.updateModel(event, value);
        this.updateViewDate(event, value.length ? value[0] : value);
      }
    } catch (err) {
      //this.updateModel(event, rawValue);
      //invalid date
      this.updateModel(event, null);
    }

    if (this.props.onInput) {
      this.props.onInput(event);
    }
  }

  isValidSelection(value) {
    let isValid = true;

    if (this.isSingleSelection()) {
      if (!(this.isSelectable(value.getDate(), value.getMonth(), value.getFullYear(), false) && this.isSelectableTime(value))) {
        isValid = false;
      }
    } else if (value.every(v => this.isSelectable(v.getDate(), v.getMonth(), v.getFullYear(), false) && this.isSelectableTime(value))) {
      if (this.isRangeSelection()) {
        isValid = value.length > 1 && value[1] > value[0] ? true : false;
      }
    }

    return isValid;
  }

  onButtonClick(event) {
    if (!this.panel.offsetParent) {
      this.showOverlay();
    } else {
      this.hideOverlay();
    }
  }

  onPrevButtonClick(event) {
    this.navigation = {
      backward: true,
      button: true
    };
    this.navBackward(event);
  }

  onNextButtonClick(event) {
    this.navigation = {
      backward: false,
      button: true
    };
    this.navForward(event);
  }

  onContainerButtonKeydown(event) {
    switch (event.which) {
      //tab
      case 9:
        this.trapFocus(event);
        break;
      //escape

      case 27:
        this.hideOverlay();
        event.preventDefault();
        break;

      default:
        //Noop
        break;
    }
  }

  trapFocus(event) {
    event.preventDefault();
    let focusableElements = DomHandler.getFocusableElements(this.panel);

    if (focusableElements && focusableElements.length > 0) {
      if (!document.activeElement) {
        focusableElements[0].focus();
      } else {
        let focusedIndex = focusableElements.indexOf(document.activeElement);

        if (event.shiftKey) {
          if (focusedIndex === -1 || focusedIndex === 0) focusableElements[focusableElements.length - 1].focus();else focusableElements[focusedIndex - 1].focus();
        } else {
          if (focusedIndex === -1 || focusedIndex === focusableElements.length - 1) focusableElements[0].focus();else focusableElements[focusedIndex + 1].focus();
        }
      }
    }
  }

  updateFocus() {
    let cell;

    if (this.navigation) {
      if (this.navigation.button) {
        this.initFocusableCell();
        if (this.navigation.backward) DomHandler.findSingle(this.panel, '.p-datepicker-prev').focus();else DomHandler.findSingle(this.panel, '.p-datepicker-next').focus();
      } else {
        if (this.navigation.backward) {
          let cells = DomHandler.find(this.panel, '.p-datepicker-calendar td span:not(.p-disabled)');
          cell = cells[cells.length - 1];
        } else {
          cell = DomHandler.findSingle(this.panel, '.p-datepicker-calendar td span:not(.p-disabled)');
        }

        if (cell) {
          cell.tabIndex = '0';
          cell.focus();
        }
      }

      this.navigation = null;
    } else {
      this.initFocusableCell();
    }
  }

  initFocusableCell() {
    let cell;

    if (this.view === 'month') {
      let cells = DomHandler.find(this.panel, '.p-monthpicker .p-monthpicker-month');
      let selectedCell = DomHandler.findSingle(this.panel, '.p-monthpicker .p-monthpicker-month.p-highlight');
      cells.forEach(cell => cell.tabIndex = -1);
      cell = selectedCell || cells[0];
    } else {
      cell = DomHandler.findSingle(this.panel, 'span.p-highlight');

      if (!cell) {
        let todayCell = DomHandler.findSingle(this.panel, 'td.p-datepicker-today span:not(.p-disabled)');
        if (todayCell) cell = todayCell;else cell = DomHandler.findSingle(this.panel, '.p-datepicker-calendar td span:not(.p-disabled)');
      }
    }

    if (cell) {
      cell.tabIndex = '0';
    }
  }

  navBackward(event) {
    if (this.props.disabled) {
      event.preventDefault();
      return;
    }

    let newViewDate = new Date(this.getViewDate().getTime());
    newViewDate.setDate(1);

    if (this.props.view === 'date') {
      if (newViewDate.getMonth() === 0) {
        newViewDate.setMonth(11);
        newViewDate.setFullYear(newViewDate.getFullYear() - 1);
      } else {
        newViewDate.setMonth(newViewDate.getMonth() - 1);
      }
    } else if (this.props.view === 'month') {
      let currentYear = newViewDate.getFullYear();
      let newYear = currentYear - 1;

      if (this.props.yearNavigator) {
        const minYear = parseInt(this.props.yearRange.split(':')[0], 10);

        if (newYear < minYear) {
          newYear = minYear;
        }
      }

      newViewDate.setFullYear(newYear);
    }

    this.updateViewDate(event, newViewDate);
    event.preventDefault();
  }

  navForward(event) {
    if (this.props.disabled) {
      event.preventDefault();
      return;
    }

    let newViewDate = new Date(this.getViewDate().getTime());
    newViewDate.setDate(1);

    if (this.props.view === 'date') {
      if (newViewDate.getMonth() === 11) {
        newViewDate.setMonth(0);
        newViewDate.setFullYear(newViewDate.getFullYear() + 1);
      } else {
        newViewDate.setMonth(newViewDate.getMonth() + 1);
      }
    } else if (this.props.view === 'month') {
      let currentYear = newViewDate.getFullYear();
      let newYear = currentYear + 1;

      if (this.props.yearNavigator) {
        const maxYear = parseInt(this.props.yearRange.split(':')[1], 10);

        if (newYear > maxYear) {
          newYear = maxYear;
        }
      }

      newViewDate.setFullYear(newYear);
    }

    this.updateViewDate(event, newViewDate);
    event.preventDefault();
  }

  onMonthDropdownChange(event) {
    const currentViewDate = this.getViewDate();
    let newViewDate = new Date(currentViewDate.getTime());
    newViewDate.setMonth(parseInt(event.target.value, 10));
    this.updateViewDate(event, newViewDate);
  }

  onYearDropdownChange(event) {
    const currentViewDate = this.getViewDate();
    let newViewDate = new Date(currentViewDate.getTime());
    newViewDate.setFullYear(parseInt(event.target.value, 10));
    this.updateViewDate(event, newViewDate);
  }

  onTodayButtonClick(event) {
    const today = new Date();
    const dateMeta = {
      day: today.getDate(),
      month: today.getMonth(),
      year: today.getFullYear(),
      today: true,
      selectable: true
    };
    const timeMeta = {
      hours: today.getHours(),
      minutes: today.getMinutes(),
      seconds: today.getSeconds(),
      milliseconds: today.getMilliseconds()
    };
    this.updateViewDate(event, today);
    this.onDateSelect(event, dateMeta, timeMeta);

    if (this.props.onTodayButtonClick) {
      this.props.onTodayButtonClick(event);
    }
  }

  onClearButtonClick(event) {
    this.updateModel(event, null);
    this.updateInputfield(null);

    if (this.props.onClearButtonClick) {
      this.props.onClearButtonClick(event);
    }
  }

  onTimePickerElementMouseDown(event, type, direction) {
    if (!this.props.disabled) {
      this.repeat(event, null, type, direction);
      event.preventDefault();
    }
  }

  onTimePickerElementMouseUp() {
    if (!this.props.disabled) {
      this.clearTimePickerTimer();
    }
  }

  onTimePickerElementMouseLeave() {
    if (!this.props.disabled) {
      this.clearTimePickerTimer();
    }
  }

  repeat(event, interval, type, direction) {
    event.persist();
    let i = interval || 500;
    this.clearTimePickerTimer();
    this.timePickerTimer = setTimeout(() => {
      this.repeat(event, 100, type, direction);
    }, i);

    switch (type) {
      case 0:
        if (direction === 1) this.incrementHour(event);else this.decrementHour(event);
        break;

      case 1:
        if (direction === 1) this.incrementMinute(event);else this.decrementMinute(event);
        break;

      case 2:
        if (direction === 1) this.incrementSecond(event);else this.decrementSecond(event);
        break;

      case 3:
        if (direction === 1) this.incrementMilliSecond(event);else this.decrementMilliSecond(event);
        break;

      default:
        break;
    }
  }

  clearTimePickerTimer() {
    if (this.timePickerTimer) {
      clearTimeout(this.timePickerTimer);
    }
  }

  incrementHour(event) {
    const currentTime = this.props.value && this.props.value instanceof Date ? this.props.value : this.getViewDate();
    const currentHour = currentTime.getHours();
    let newHour = currentHour + this.props.stepHour;
    newHour = newHour >= 24 ? newHour - 24 : newHour;

    if (this.validateHour(newHour, currentTime)) {
      if (this.props.maxDate && this.props.maxDate.toDateString() === currentTime.toDateString() && this.props.maxDate.getHours() === newHour) {
        if (this.props.maxDate.getMinutes() < currentTime.getMinutes()) {
          if (this.props.maxDate.getSeconds() < currentTime.getSeconds()) {
            if (this.props.maxDate.getMilliseconds() < currentTime.getMilliseconds()) {
              this.updateTime(event, newHour, this.props.maxDate.getMinutes(), this.props.maxDate.getSeconds(), this.props.maxDate.getMilliseconds());
            } else {
              this.updateTime(event, newHour, this.props.maxDate.getMinutes(), this.props.maxDate.getSeconds(), currentTime.getMilliseconds());
            }
          } else {
            this.updateTime(event, newHour, this.props.maxDate.getMinutes(), currentTime.getSeconds(), currentTime.getMilliseconds());
          }
        } else if (this.props.maxDate.getMinutes() === currentTime.getMinutes()) {
          if (this.props.maxDate.getSeconds() < currentTime.getSeconds()) {
            if (this.props.maxDate.getMilliseconds() < currentTime.getMilliseconds()) {
              this.updateTime(event, newHour, this.props.maxDate.getMinutes(), this.props.maxDate.getSeconds(), this.props.maxDate.getMilliseconds());
            } else {
              this.updateTime(event, newHour, this.props.maxDate.getMinutes(), this.props.maxDate.getSeconds(), currentTime.getMilliseconds());
            }
          } else {
            this.updateTime(event, newHour, this.props.maxDate.getMinutes(), currentTime.getSeconds(), currentTime.getMilliseconds());
          }
        } else {
          this.updateTime(event, newHour, currentTime.getMinutes(), currentTime.getSeconds(), currentTime.getMilliseconds());
        }
      } else {
        this.updateTime(event, newHour, currentTime.getMinutes(), currentTime.getSeconds(), currentTime.getMilliseconds());
      }
    }

    event.preventDefault();
  }

  decrementHour(event) {
    const currentTime = this.props.value && this.props.value instanceof Date ? this.props.value : this.getViewDate();
    const currentHour = currentTime.getHours();
    let newHour = currentHour - this.props.stepHour;
    newHour = newHour < 0 ? newHour + 24 : newHour;

    if (this.validateHour(newHour, currentTime)) {
      if (this.props.minDate && this.props.minDate.toDateString() === currentTime.toDateString() && this.props.minDate.getHours() === newHour) {
        if (this.props.minDate.getMinutes() > currentTime.getMinutes()) {
          if (this.props.minDate.getSeconds() > currentTime.getSeconds()) {
            if (this.props.minDate.getMilliseconds() > currentTime.getMilliseconds()) {
              this.updateTime(event, newHour, this.props.minDate.getMinutes(), this.props.minDate.getSeconds(), this.props.minDate.getMilliseconds());
            } else {
              this.updateTime(event, newHour, this.props.minDate.getMinutes(), this.props.minDate.getSeconds(), currentTime.getMilliseconds());
            }
          } else {
            this.updateTime(event, newHour, this.props.minDate.getMinutes(), currentTime.getSeconds(), currentTime.getMilliseconds());
          }
        } else if (this.props.minDate.getMinutes() === currentTime.getMinutes()) {
          if (this.props.minDate.getSeconds() > currentTime.getSeconds()) {
            if (this.props.minDate.getMilliseconds() > currentTime.getMilliseconds()) {
              this.updateTime(event, newHour, this.props.minDate.getMinutes(), this.props.minDate.getSeconds(), this.props.minDate.getMilliseconds());
            } else {
              this.updateTime(event, newHour, this.props.minDate.getMinutes(), this.props.minDate.getSeconds(), currentTime.getMilliseconds());
            }
          } else {
            this.updateTime(event, newHour, this.props.minDate.getMinutes(), currentTime.getSeconds(), currentTime.getMilliseconds());
          }
        } else {
          this.updateTime(event, newHour, currentTime.getMinutes(), currentTime.getSeconds(), currentTime.getMilliseconds());
        }
      } else {
        this.updateTime(event, newHour, currentTime.getMinutes(), currentTime.getSeconds(), currentTime.getMilliseconds());
      }
    }

    event.preventDefault();
  }

  incrementMinute(event) {
    const currentTime = this.props.value && this.props.value instanceof Date ? this.props.value : this.getViewDate();
    const currentMinute = currentTime.getMinutes();
    let newMinute = currentMinute + this.props.stepMinute;
    newMinute = newMinute > 59 ? newMinute - 60 : newMinute;

    if (this.validateMinute(newMinute, currentTime)) {
      if (this.props.maxDate && this.props.maxDate.toDateString() === currentTime.toDateString() && this.props.maxDate.getMinutes() === newMinute) {
        if (this.props.maxDate.getSeconds() < currentTime.getSeconds()) {
          if (this.props.maxDate.getMilliseconds() < currentTime.getMilliseconds()) {
            this.updateTime(event, currentTime.getHours(), newMinute, this.props.maxDate.getSeconds(), this.props.maxDate.getMilliseconds());
          } else {
            this.updateTime(event, currentTime.getHours(), newMinute, this.props.maxDate.getSeconds(), currentTime.getMilliseconds());
          }
        } else {
          this.updateTime(event, currentTime.getHours(), newMinute, currentTime.getSeconds(), currentTime.getMilliseconds());
        }
      } else {
        this.updateTime(event, currentTime.getHours(), newMinute, currentTime.getSeconds(), currentTime.getMilliseconds());
      }
    }

    event.preventDefault();
  }

  decrementMinute(event) {
    const currentTime = this.props.value && this.props.value instanceof Date ? this.props.value : this.getViewDate();
    const currentMinute = currentTime.getMinutes();
    let newMinute = currentMinute - this.props.stepMinute;
    newMinute = newMinute < 0 ? newMinute + 60 : newMinute;

    if (this.validateMinute(newMinute, currentTime)) {
      if (this.props.minDate && this.props.minDate.toDateString() === currentTime.toDateString() && this.props.minDate.getMinutes() === newMinute) {
        if (this.props.minDate.getSeconds() > currentTime.getSeconds()) {
          if (this.props.minDate.getMilliseconds() > currentTime.getMilliseconds()) {
            this.updateTime(event, currentTime.getHours(), newMinute, this.props.minDate.getSeconds(), this.props.minDate.getMilliseconds());
          } else {
            this.updateTime(event, currentTime.getHours(), newMinute, this.props.minDate.getSeconds(), currentTime.getMilliseconds());
          }
        } else {
          this.updateTime(event, currentTime.getHours(), newMinute, currentTime.getSeconds(), currentTime.getMilliseconds());
        }
      } else {
        this.updateTime(event, currentTime.getHours(), newMinute, currentTime.getSeconds(), currentTime.getMilliseconds());
      }
    }

    event.preventDefault();
  }

  incrementSecond(event) {
    const currentTime = this.props.value && this.props.value instanceof Date ? this.props.value : this.getViewDate();
    const currentSecond = currentTime.getSeconds();
    let newSecond = currentSecond + this.props.stepSecond;
    newSecond = newSecond > 59 ? newSecond - 60 : newSecond;

    if (this.validateSecond(newSecond, currentTime)) {
      if (this.props.maxDate && this.props.maxDate.toDateString() === currentTime.toDateString() && this.props.maxDate.getSeconds() === newSecond) {
        if (this.props.maxDate.getMilliseconds() < currentTime.getMilliseconds()) {
          this.updateTime(event, currentTime.getHours(), currentTime.getMinutes(), newSecond, this.props.maxDate.getMilliseconds());
        } else {
          this.updateTime(event, currentTime.getHours(), currentTime.getMinutes(), newSecond, currentTime.getMilliseconds());
        }
      } else {
        this.updateTime(event, currentTime.getHours(), currentTime.getMinutes(), newSecond, currentTime.getMilliseconds());
      }
    }

    event.preventDefault();
  }

  decrementSecond(event) {
    const currentTime = this.props.value && this.props.value instanceof Date ? this.props.value : this.getViewDate();
    const currentSecond = currentTime.getSeconds();
    let newSecond = currentSecond - this.props.stepSecond;
    newSecond = newSecond < 0 ? newSecond + 60 : newSecond;

    if (this.validateSecond(newSecond, currentTime)) {
      if (this.props.minDate && this.props.minDate.toDateString() === currentTime.toDateString() && this.props.minDate.getSeconds() === newSecond) {
        if (this.props.minDate.getMilliseconds() > currentTime.getMilliseconds()) {
          this.updateTime(event, currentTime.getHours(), currentTime.getMinutes(), newSecond, this.props.minDate.getMilliseconds());
        } else {
          this.updateTime(event, currentTime.getHours(), currentTime.getMinutes(), newSecond, currentTime.getMilliseconds());
        }
      } else {
        this.updateTime(event, currentTime.getHours(), currentTime.getMinutes(), newSecond, currentTime.getMilliseconds());
      }
    }

    event.preventDefault();
  }

  incrementMilliSecond(event) {
    const currentTime = this.props.value && this.props.value instanceof Date ? this.props.value : this.getViewDate();
    const currentMillisecond = currentTime.getMilliseconds();
    let newMillisecond = currentMillisecond + this.props.stepMillisec;
    newMillisecond = newMillisecond > 999 ? newMillisecond - 1000 : newMillisecond;

    if (this.validateMillisecond(newMillisecond, currentTime)) {
      this.updateTime(event, currentTime.getHours(), currentTime.getMinutes(), currentTime.getSeconds(), newMillisecond);
    }

    event.preventDefault();
  }

  decrementMilliSecond(event) {
    const currentTime = this.props.value && this.props.value instanceof Date ? this.props.value : this.getViewDate();
    const currentMillisecond = currentTime.getMilliseconds();
    let newMillisecond = currentMillisecond - this.props.stepMillisec;
    newMillisecond = newMillisecond < 0 ? newMillisecond + 200 : newMillisecond;

    if (this.validateMillisecond(newMillisecond, currentTime)) {
      this.updateTime(event, currentTime.getHours(), currentTime.getMinutes(), currentTime.getSeconds(), newMillisecond);
    }

    event.preventDefault();
  }

  toggleAmPm(event) {
    const currentTime = this.props.value && this.props.value instanceof Date ? this.props.value : this.getViewDate();
    const currentHour = currentTime.getHours();
    const newHour = currentHour >= 12 ? currentHour - 12 : currentHour + 12;
    this.updateTime(event, newHour, currentTime.getMinutes(), currentTime.getSeconds(), currentTime.getMilliseconds());
    event.preventDefault();
  }

  getViewDate() {
    return this.props.onViewDateChange ? this.props.viewDate : this.state.viewDate;
  }

  isValidDate(date) {
    return date instanceof Date && !isNaN(date);
  }

  validateHour(hour, value) {
    let valid = true;
    let valueDateString = value ? value.toDateString() : null;

    if (this.props.minDate && valueDateString && this.props.minDate.toDateString() === valueDateString) {
      if (this.props.minDate.getHours() > hour) {
        valid = false;
      }
    }

    if (this.props.maxDate && valueDateString && this.props.maxDate.toDateString() === valueDateString) {
      if (this.props.maxDate.getHours() < hour) {
        valid = false;
      }
    }

    return valid;
  }

  validateMinute(minute, value) {
    let valid = true;
    let valueDateString = value ? value.toDateString() : null;

    if (this.props.minDate && valueDateString && this.props.minDate.toDateString() === valueDateString) {
      if (value.getHours() === this.props.minDate.getHours()) {
        if (this.props.minDate.getMinutes() > minute) {
          valid = false;
        }
      }
    }

    if (this.props.maxDate && valueDateString && this.props.maxDate.toDateString() === valueDateString) {
      if (value.getHours() === this.props.maxDate.getHours()) {
        if (this.props.maxDate.getMinutes() < minute) {
          valid = false;
        }
      }
    }

    return valid;
  }

  validateSecond(second, value) {
    let valid = true;
    let valueDateString = value ? value.toDateString() : null;

    if (this.props.minDate && valueDateString && this.props.minDate.toDateString() === valueDateString) {
      if (value.getHours() === this.props.minDate.getHours() && value.getMinutes() === this.props.minDate.getMinutes()) {
        if (this.props.minDate.getSeconds() > second) {
          valid = false;
        }
      }
    }

    if (this.props.maxDate && valueDateString && this.props.maxDate.toDateString() === valueDateString) {
      if (value.getHours() === this.props.maxDate.getHours() && value.getMinutes() === this.props.maxDate.getMinutes()) {
        if (this.props.maxDate.getSeconds() < second) {
          valid = false;
        }
      }
    }

    return valid;
  }

  validateMillisecond(millisecond, value) {
    let valid = true;
    let valueDateString = value ? value.toDateString() : null;

    if (this.props.minDate && valueDateString && this.props.minDate.toDateString() === valueDateString) {
      if (value.getHours() === this.props.minDate.getHours() && value.getSeconds() === this.props.minDate.getSeconds() && value.getMinutes() === this.props.minDate.getMinutes()) {
        if (this.props.minDate.getMilliseconds() > millisecond) {
          valid = false;
        }
      }
    }

    if (this.props.maxDate && valueDateString && this.props.maxDate.toDateString() === valueDateString) {
      if (value.getHours() === this.props.maxDate.getHours() && value.getSeconds() === this.props.maxDate.getSeconds() && value.getMinutes() === this.props.maxDate.getMinutes()) {
        if (this.props.maxDate.getMilliseconds() < millisecond) {
          valid = false;
        }
      }
    }

    return valid;
  }

  updateTime(event, hour, minute, second, millisecond) {
    let newDateTime = this.props.value && this.props.value instanceof Date ? new Date(this.props.value) : new Date();
    newDateTime.setHours(hour);
    newDateTime.setMinutes(minute);
    newDateTime.setSeconds(second);
    newDateTime.setMilliseconds(millisecond);
    this.updateModel(event, newDateTime);

    if (this.props.onSelect) {
      this.props.onSelect({
        originalEvent: event,
        value: newDateTime
      });
    }

    this.updateInputfield(newDateTime);
  }

  updateViewDate(event, value) {
    if (this.props.yearNavigator) {
      let viewYear = value.getFullYear();

      if (this.props.minDate && this.props.minDate.getFullYear() > viewYear) {
        viewYear = this.props.minDate.getFullYear();
      }

      if (this.props.maxDate && this.props.maxDate.getFullYear() < viewYear) {
        viewYear = this.props.maxDate.getFullYear();
      }

      value.setFullYear(viewYear);
    }

    if (this.props.monthNavigator && this.props.view !== 'month') {
      let viewMonth = value.getMonth();
      let viewMonthWithMinMax = parseInt(this.isInMinYear(value) && Math.max(this.props.minDate.getMonth(), viewMonth).toString() || this.isInMaxYear(value) && Math.min(this.props.maxDate.getMonth(), viewMonth).toString() || viewMonth);
      value.setMonth(viewMonthWithMinMax);
    }

    if (this.props.onViewDateChange) {
      this.props.onViewDateChange({
        originalEvent: event,
        value: value
      });
    } else {
      this.viewStateChanged = true;
      this.setState({
        viewDate: value
      });
    }
  }

  onDateCellKeydown(event, date, groupIndex) {
    const cellContent = event.currentTarget;
    const cell = cellContent.parentElement;

    switch (event.which) {
      //down arrow
      case 40:
        {
          cellContent.tabIndex = '-1';
          let cellIndex = DomHandler.index(cell);
          let nextRow = cell.parentElement.nextElementSibling;

          if (nextRow) {
            let focusCell = nextRow.children[cellIndex].children[0];

            if (DomHandler.hasClass(focusCell, 'p-disabled')) {
              this.navigation = {
                backward: false
              };
              this.navForward(event);
            } else {
              nextRow.children[cellIndex].children[0].tabIndex = '0';
              nextRow.children[cellIndex].children[0].focus();
            }
          } else {
            this.navigation = {
              backward: false
            };
            this.navForward(event);
          }

          event.preventDefault();
          break;
        }
      //up arrow

      case 38:
        {
          cellContent.tabIndex = '-1';
          let cellIndex = DomHandler.index(cell);
          let prevRow = cell.parentElement.previousElementSibling;

          if (prevRow) {
            let focusCell = prevRow.children[cellIndex].children[0];

            if (DomHandler.hasClass(focusCell, 'p-disabled')) {
              this.navigation = {
                backward: true
              };
              this.navBackward(event);
            } else {
              focusCell.tabIndex = '0';
              focusCell.focus();
            }
          } else {
            this.navigation = {
              backward: true
            };
            this.navBackward(event);
          }

          event.preventDefault();
          break;
        }
      //left arrow

      case 37:
        {
          cellContent.tabIndex = '-1';
          let prevCell = cell.previousElementSibling;

          if (prevCell) {
            let focusCell = prevCell.children[0];

            if (DomHandler.hasClass(focusCell, 'p-disabled')) {
              this.navigateToMonth(true, groupIndex, event);
            } else {
              focusCell.tabIndex = '0';
              focusCell.focus();
            }
          } else {
            this.navigateToMonth(true, groupIndex, event);
          }

          event.preventDefault();
          break;
        }
      //right arrow

      case 39:
        {
          cellContent.tabIndex = '-1';
          let nextCell = cell.nextElementSibling;

          if (nextCell) {
            let focusCell = nextCell.children[0];

            if (DomHandler.hasClass(focusCell, 'p-disabled')) {
              this.navigateToMonth(false, groupIndex, event);
            } else {
              focusCell.tabIndex = '0';
              focusCell.focus();
            }
          } else {
            this.navigateToMonth(false, groupIndex, event);
          }

          event.preventDefault();
          break;
        }
      //enter

      case 13:
        {
          this.onDateSelect(event, date);
          event.preventDefault();
          break;
        }
      //escape

      case 27:
        {
          this.hideOverlay();
          event.preventDefault();
          break;
        }
      //tab

      case 9:
        {
          this.trapFocus(event);
          break;
        }

      default:
        //no op
        break;
    }
  }

  navigateToMonth(prev, groupIndex, event) {
    if (prev) {
      if (this.props.numberOfMonths === 1 || groupIndex === 0) {
        this.navigation = {
          backward: true
        };
        this.navBackward(event);
      } else {
        let prevMonthContainer = this.panel.children[groupIndex - 1];
        let cells = DomHandler.find(prevMonthContainer, '.p-datepicker-calendar td span:not(.p-disabled)');
        let focusCell = cells[cells.length - 1];
        focusCell.tabIndex = '0';
        focusCell.focus();
      }
    } else {
      if (this.props.numberOfMonths === 1 || groupIndex === this.props.numberOfMonths - 1) {
        this.navigation = {
          backward: false
        };
        this.navForward(event);
      } else {
        let nextMonthContainer = this.panel.children[groupIndex + 1];
        let focusCell = DomHandler.findSingle(nextMonthContainer, '.p-datepicker-calendar td span:not(.p-disabled)');
        focusCell.tabIndex = '0';
        focusCell.focus();
      }
    }
  }

  onMonthCellKeydown(event, index) {
    const cell = event.currentTarget;

    switch (event.which) {
      //arrows
      case 38:
      case 40:
        {
          cell.tabIndex = '-1';
          var cells = cell.parentElement.children;
          var cellIndex = DomHandler.index(cell);
          let nextCell = cells[event.which === 40 ? cellIndex + 3 : cellIndex - 3];

          if (nextCell) {
            nextCell.tabIndex = '0';
            nextCell.focus();
          }

          event.preventDefault();
          break;
        }
      //left arrow

      case 37:
        {
          cell.tabIndex = '-1';
          let prevCell = cell.previousElementSibling;

          if (prevCell) {
            prevCell.tabIndex = '0';
            prevCell.focus();
          }

          event.preventDefault();
          break;
        }
      //right arrow

      case 39:
        {
          cell.tabIndex = '-1';
          let nextCell = cell.nextElementSibling;

          if (nextCell) {
            nextCell.tabIndex = '0';
            nextCell.focus();
          }

          event.preventDefault();
          break;
        }
      //enter

      case 13:
        {
          this.onMonthSelect(event, index);
          event.preventDefault();
          break;
        }
      //escape

      case 27:
        {
          this.hideOverlay();
          event.preventDefault();
          break;
        }
      //tab

      case 9:
        {
          this.trapFocus(event);
          break;
        }

      default:
        //no op
        break;
    }
  }

  onDateSelect(event, dateMeta, timeMeta) {
    if (this.props.disabled || !dateMeta.selectable) {
      event.preventDefault();
      return;
    }

    DomHandler.find(this.panel, '.p-datepicker-calendar td span:not(.p-disabled)').forEach(cell => cell.tabIndex = -1);
    event.currentTarget.focus();

    if (this.isMultipleSelection()) {
      if (this.isSelected(dateMeta)) {
        let value = this.props.value.filter((date, i) => {
          return !this.isDateEquals(date, dateMeta);
        });
        this.updateModel(event, value);
      } else if (!this.props.maxDateCount || !this.props.value || this.props.maxDateCount > this.props.value.length) {
        this.selectDate(event, dateMeta, timeMeta);
      }
    } else {
      this.selectDate(event, dateMeta, timeMeta);
    }

    if (!this.props.inline && this.isSingleSelection() && (!this.props.showTime || this.props.hideOnDateTimeSelect)) {
      setTimeout(() => {
        this.hideOverlay();
      }, 100);

      if (this.mask) {
        this.disableModality();
      }
    }

    event.preventDefault();
  }

  selectTime(date, timeMeta) {
    if (this.props.showTime) {
      let hours, minutes, seconds, milliseconds;

      if (timeMeta) {
        ({
          hours,
          minutes,
          seconds,
          milliseconds
        } = timeMeta);
      } else {
        let time = this.props.value && this.props.value instanceof Date ? this.props.value : new Date();
        [hours, minutes, seconds, milliseconds] = [time.getHours(), time.getMinutes(), time.getSeconds(), time.getMilliseconds()];
      }

      date.setHours(hours);
      date.setMinutes(minutes);
      date.setSeconds(seconds);
      date.setMilliseconds(milliseconds);
    }
  }

  selectDate(event, dateMeta, timeMeta) {
    let date = new Date(dateMeta.year, dateMeta.month, dateMeta.day);
    this.selectTime(date, timeMeta);

    if (this.props.minDate && this.props.minDate > date) {
      date = this.props.minDate;
    }

    if (this.props.maxDate && this.props.maxDate < date) {
      date = this.props.maxDate;
    }

    let selectedValues = date;

    if (this.isSingleSelection()) {
      this.updateModel(event, date);
    } else if (this.isMultipleSelection()) {
      selectedValues = this.props.value ? [...this.props.value, date] : [date];
      this.updateModel(event, selectedValues);
    } else if (this.isRangeSelection()) {
      if (this.props.value && this.props.value.length) {
        let startDate = this.props.value[0];
        let endDate = this.props.value[1];

        if (!endDate && date.getTime() >= startDate.getTime()) {
          endDate = date;
        } else {
          startDate = date;
          endDate = null;
        }

        selectedValues = [startDate, endDate];
        this.updateModel(event, selectedValues);
      } else {
        selectedValues = [date, null];
        this.updateModel(event, selectedValues);
      }
    }

    if (this.props.onSelect) {
      this.props.onSelect({
        originalEvent: event,
        value: date
      });
    }

    this.updateInputfield(selectedValues);
  }

  onMonthSelect(event, month) {
    this.onDateSelect(event, {
      year: this.getViewDate().getFullYear(),
      month: month,
      day: 1,
      selectable: true
    });
    event.preventDefault();
  }

  updateModel(event, value) {
    if (this.props.onChange) {
      this.props.onChange({
        originalEvent: event,
        value: value,
        stopPropagation: () => {},
        preventDefault: () => {},
        target: {
          name: this.props.name,
          id: this.props.id,
          value: value
        }
      });
      this.viewStateChanged = true;
    }
  }

  showOverlay() {
    if (this.props.autoZIndex) {
      this.panel.style.zIndex = String(this.props.baseZIndex + DomHandler.generateZIndex());
    }

    this.panel.style.display = 'block';
    setTimeout(() => {
      DomHandler.addClass(this.panel, 'p-input-overlay-visible');
      DomHandler.removeClass(this.panel, 'p-input-overlay-hidden');
    }, 1);
    this.alignPanel();
    this.bindDocumentClickListener();
    this.bindDocumentResizeListener();
  }

  hideOverlay() {
    if (this.panel) {
      DomHandler.addClass(this.panel, 'p-input-overlay-hidden');
      DomHandler.removeClass(this.panel, 'p-input-overlay-visible');
      this.unbindDocumentClickListener();
      this.unbindDocumentResizeListener();
      this.hideTimeout = setTimeout(() => {
        this.panel.style.display = 'none';
        DomHandler.removeClass(this.panel, 'p-input-overlay-hidden');
      }, 150);
    }
  }

  bindDocumentClickListener() {
    if (!this.documentClickListener) {
      this.documentClickListener = event => {
        if (this.isOutsideClicked(event)) {
          this.hideOverlay();
        }
      };

      document.addEventListener('mousedown', this.documentClickListener);
    }
  }

  unbindDocumentClickListener() {
    if (this.documentClickListener) {
      document.removeEventListener('mousedown', this.documentClickListener);
      this.documentClickListener = null;
    }
  }

  bindDocumentResizeListener() {
    if (!this.documentResizeListener && !this.props.touchUI) {
      this.documentResizeListener = this.onWindowResize.bind(this);
      window.addEventListener('resize', this.documentResizeListener);
    }
  }

  unbindDocumentResizeListener() {
    if (this.documentResizeListener) {
      window.removeEventListener('resize', this.documentResizeListener);
      this.documentResizeListener = null;
    }
  }

  isOutsideClicked(event) {
    return this.container && !(this.container.isSameNode(event.target) || this.isNavIconClicked(event) || this.container.contains(event.target) || this.panel && this.panel.contains(event.target));
  }

  isNavIconClicked(event) {
    return DomHandler.hasClass(event.target, 'p-datepicker-prev') || DomHandler.hasClass(event.target, 'p-datepicker-prev-icon') || DomHandler.hasClass(event.target, 'p-datepicker-next') || DomHandler.hasClass(event.target, 'p-datepicker-next-icon');
  }

  onWindowResize() {
    if (this.panel.offsetParent && !DomHandler.isAndroid()) {
      this.hideOverlay();
    }
  }

  alignPanel() {
    if (this.props.touchUI) {
      this.enableModality();
    } else {
      if (this.props.appendTo) {
        DomHandler.absolutePosition(this.panel, this.inputElement);
        this.panel.style.minWidth = DomHandler.getWidth(this.container) + 'px';
      } else {
        DomHandler.relativePosition(this.panel, this.inputElement);
      }
    }
  }

  enableModality() {
    if (!this.mask) {
      this.mask = document.createElement('div');
      this.mask.style.zIndex = String(parseInt(this.panel.style.zIndex, 10) - 1);
      DomHandler.addMultipleClasses(this.mask, 'p-component-overlay p-datepicker-mask p-datepicker-mask-scrollblocker');

      this.maskClickListener = () => {
        this.disableModality();
      };

      this.mask.addEventListener('click', this.maskClickListener);
      document.body.appendChild(this.mask);
      DomHandler.addClass(document.body, 'p-overflow-hidden');
    }
  }

  disableModality() {
    if (this.mask) {
      this.mask.removeEventListener('click', this.maskClickListener);
      this.maskClickListener = null;
      document.body.removeChild(this.mask);
      this.mask = null;
      let bodyChildren = document.body.children;
      let hasBlockerMasks;

      for (let i = 0; i < bodyChildren.length; i++) {
        let bodyChild = bodyChildren[i];

        if (DomHandler.hasClass(bodyChild, 'p-datepicker-mask-scrollblocker')) {
          hasBlockerMasks = true;
          break;
        }
      }

      if (!hasBlockerMasks) {
        DomHandler.removeClass(document.body, 'p-overflow-hidden');
      }

      this.hideOverlay();
    }
  }

  getFirstDayOfMonthIndex(month, year) {
    let day = new Date();
    day.setDate(1);
    day.setMonth(month);
    day.setFullYear(year);
    let dayIndex = day.getDay() + this.getSundayIndex();
    return dayIndex >= 7 ? dayIndex - 7 : dayIndex;
  }

  getDaysCountInMonth(month, year) {
    return 32 - this.daylightSavingAdjust(new Date(year, month, 32)).getDate();
  }

  getDaysCountInPrevMonth(month, year) {
    let prev = this.getPreviousMonthAndYear(month, year);
    return this.getDaysCountInMonth(prev.month, prev.year);
  }

  daylightSavingAdjust(date) {
    if (!date) {
      return null;
    }

    date.setHours(date.getHours() > 12 ? date.getHours() + 2 : 0);
    return date;
  }

  getPreviousMonthAndYear(month, year) {
    let m, y;

    if (month === 0) {
      m = 11;
      y = year - 1;
    } else {
      m = month - 1;
      y = year;
    }

    return {
      'month': m,
      'year': y
    };
  }

  getNextMonthAndYear(month, year) {
    let m, y;

    if (month === 11) {
      m = 0;
      y = year + 1;
    } else {
      m = month + 1;
      y = year;
    }

    return {
      'month': m,
      'year': y
    };
  }

  getSundayIndex() {
    return this.props.locale.firstDayOfWeek > 0 ? 7 - this.props.locale.firstDayOfWeek : 0;
  }

  createWeekDays() {
    let weekDays = [];
    let dayIndex = this.props.locale.firstDayOfWeek;

    for (let i = 0; i < 7; i++) {
      weekDays.push(this.props.locale.dayNamesMin[dayIndex]);
      dayIndex = dayIndex === 6 ? 0 : ++dayIndex;
    }

    return weekDays;
  }

  createMonths(month, year) {
    let months = [];

    for (let i = 0; i < this.props.numberOfMonths; i++) {
      let m = month + i;
      let y = year;

      if (m > 11) {
        m = m % 11 - 1;
        y = year + 1;
      }

      months.push(this.createMonth(m, y));
    }

    return months;
  }

  createMonth(month, year) {
    let dates = [];
    let firstDay = this.getFirstDayOfMonthIndex(month, year);
    let daysLength = this.getDaysCountInMonth(month, year);
    let prevMonthDaysLength = this.getDaysCountInPrevMonth(month, year);
    let dayNo = 1;
    let today = new Date();
    let weekNumbers = [];
    let monthRows = Math.ceil((daysLength + firstDay) / 7);

    for (let i = 0; i < monthRows; i++) {
      let week = [];

      if (i === 0) {
        for (let j = prevMonthDaysLength - firstDay + 1; j <= prevMonthDaysLength; j++) {
          let prev = this.getPreviousMonthAndYear(month, year);
          week.push({
            day: j,
            month: prev.month,
            year: prev.year,
            otherMonth: true,
            today: this.isToday(today, j, prev.month, prev.year),
            selectable: this.isSelectable(j, prev.month, prev.year, true)
          });
        }

        let remainingDaysLength = 7 - week.length;

        for (let j = 0; j < remainingDaysLength; j++) {
          week.push({
            day: dayNo,
            month: month,
            year: year,
            today: this.isToday(today, dayNo, month, year),
            selectable: this.isSelectable(dayNo, month, year, false)
          });
          dayNo++;
        }
      } else {
        for (let j = 0; j < 7; j++) {
          if (dayNo > daysLength) {
            let next = this.getNextMonthAndYear(month, year);
            week.push({
              day: dayNo - daysLength,
              month: next.month,
              year: next.year,
              otherMonth: true,
              today: this.isToday(today, dayNo - daysLength, next.month, next.year),
              selectable: this.isSelectable(dayNo - daysLength, next.month, next.year, true)
            });
          } else {
            week.push({
              day: dayNo,
              month: month,
              year: year,
              today: this.isToday(today, dayNo, month, year),
              selectable: this.isSelectable(dayNo, month, year, false)
            });
          }

          dayNo++;
        }
      }

      if (this.props.showWeek) {
        weekNumbers.push(this.getWeekNumber(new Date(week[0].year, week[0].month, week[0].day)));
      }

      dates.push(week);
    }

    return {
      month: month,
      year: year,
      dates: dates,
      weekNumbers: weekNumbers
    };
  }

  getWeekNumber(date) {
    let checkDate = new Date(date.getTime());
    checkDate.setDate(checkDate.getDate() + 4 - (checkDate.getDay() || 7));
    let time = checkDate.getTime();
    checkDate.setMonth(0);
    checkDate.setDate(1);
    return Math.floor(Math.round((time - checkDate.getTime()) / 86400000) / 7) + 1;
  }

  isSelectable(day, month, year, otherMonth) {
    let validMin = true;
    let validMax = true;
    let validDate = true;
    let validDay = true;
    let validMonth = true;

    if (this.props.minDate) {
      if (this.props.minDate.getFullYear() > year) {
        validMin = false;
      } else if (this.props.minDate.getFullYear() === year) {
        if (this.props.minDate.getMonth() > month) {
          validMin = false;
        } else if (this.props.minDate.getMonth() === month) {
          if (this.props.minDate.getDate() > day) {
            validMin = false;
          }
        }
      }
    }

    if (this.props.maxDate) {
      if (this.props.maxDate.getFullYear() < year) {
        validMax = false;
      } else if (this.props.maxDate.getFullYear() === year) {
        if (this.props.maxDate.getMonth() < month) {
          validMax = false;
        } else if (this.props.maxDate.getMonth() === month) {
          if (this.props.maxDate.getDate() < day) {
            validMax = false;
          }
        }
      }
    }

    if (this.props.disabledDates) {
      validDate = !this.isDateDisabled(day, month, year);
    }

    if (this.props.disabledDays) {
      validDay = !this.isDayDisabled(day, month, year);
    }

    if (this.props.selectOtherMonths === false && otherMonth) {
      validMonth = false;
    }

    return validMin && validMax && validDate && validDay && validMonth;
  }

  isSelectableTime(value) {
    let validMin = true;
    let validMax = true;

    if (this.props.minDate && this.props.minDate.toDateString() === value.toDateString()) {
      if (this.props.minDate.getHours() > value.getHours()) {
        validMin = false;
      } else if (this.props.minDate.getHours() === value.getHours()) {
        if (this.props.minDate.getMinutes() > value.getMinutes()) {
          validMin = false;
        } else if (this.props.minDate.getMinutes() === value.getMinutes()) {
          if (this.props.minDate.getSeconds() > value.getSeconds()) {
            validMin = false;
          } else if (this.props.minDate.getSeconds() === value.getSeconds()) {
            if (this.props.minDate.getMilliseconds() > value.getMilliseconds()) {
              validMin = false;
            }
          }
        }
      }
    }

    if (this.props.maxDate && this.props.maxDate.toDateString() === value.toDateString()) {
      if (this.props.maxDate.getHours() < value.getHours()) {
        validMax = false;
      } else if (this.props.maxDate.getHours() === value.getHours()) {
        if (this.props.maxDate.getMinutes() < value.getMinutes()) {
          validMax = false;
        } else if (this.props.maxDate.getMinutes() === value.getMinutes()) {
          if (this.props.maxDate.getSeconds() < value.getSeconds()) {
            validMax = false;
          } else if (this.props.maxDate.getSeconds() === value.getSeconds()) {
            if (this.props.maxDate.getMilliseconds() < value.getMilliseconds()) {
              validMax = false;
            }
          }
        }
      }
    }

    return validMin && validMax;
  }

  isSelected(dateMeta) {
    if (this.props.value) {
      if (this.isSingleSelection()) {
        return this.isDateEquals(this.props.value, dateMeta);
      } else if (this.isMultipleSelection()) {
        let selected = false;

        for (let date of this.props.value) {
          selected = this.isDateEquals(date, dateMeta);

          if (selected) {
            break;
          }
        }

        return selected;
      } else if (this.isRangeSelection()) {
        if (this.props.value[1]) return this.isDateEquals(this.props.value[0], dateMeta) || this.isDateEquals(this.props.value[1], dateMeta) || this.isDateBetween(this.props.value[0], this.props.value[1], dateMeta);else {
          return this.isDateEquals(this.props.value[0], dateMeta);
        }
      }
    } else {
      return false;
    }
  }

  isMonthSelected(month) {
    const viewDate = this.getViewDate();
    if (this.props.value && this.props.value instanceof Date) return this.props.value.getDate() === 1 && this.props.value.getMonth() === month && this.props.value.getFullYear() === viewDate.getFullYear();else return false;
  }

  isDateEquals(value, dateMeta) {
    if (value && value instanceof Date) return value.getDate() === dateMeta.day && value.getMonth() === dateMeta.month && value.getFullYear() === dateMeta.year;else return false;
  }

  isDateBetween(start, end, dateMeta) {
    let between = false;

    if (start && end) {
      let date = new Date(dateMeta.year, dateMeta.month, dateMeta.day);
      return start.getTime() <= date.getTime() && end.getTime() >= date.getTime();
    }

    return between;
  }

  isSingleSelection() {
    return this.props.selectionMode === 'single';
  }

  isRangeSelection() {
    return this.props.selectionMode === 'range';
  }

  isMultipleSelection() {
    return this.props.selectionMode === 'multiple';
  }

  isToday(today, day, month, year) {
    return today.getDate() === day && today.getMonth() === month && today.getFullYear() === year;
  }

  isDateDisabled(day, month, year) {
    if (this.props.disabledDates) {
      for (let i = 0; i < this.props.disabledDates.length; i++) {
        let disabledDate = this.props.disabledDates[i];

        if (disabledDate.getFullYear() === year && disabledDate.getMonth() === month && disabledDate.getDate() === day) {
          return true;
        }
      }
    }

    return false;
  }

  isDayDisabled(day, month, year) {
    if (this.props.disabledDays) {
      let weekday = new Date(year, month, day);
      let weekdayNumber = weekday.getDay();
      return this.props.disabledDays.indexOf(weekdayNumber) !== -1;
    }

    return false;
  }

  updateInputfield(value) {
    if (!this.inputElement) {
      return;
    }

    let formattedValue = '';

    if (value) {
      try {
        if (this.isSingleSelection()) {
          formattedValue = this.isValidDate(value) ? this.formatDateTime(value) : '';
        } else if (this.isMultipleSelection()) {
          for (let i = 0; i < value.length; i++) {
            let selectedValue = value[i];
            let dateAsString = this.isValidDate(selectedValue) ? this.formatDateTime(selectedValue) : '';
            formattedValue += dateAsString;

            if (i !== value.length - 1) {
              formattedValue += ', ';
            }
          }
        } else if (this.isRangeSelection()) {
          if (value && value.length) {
            let startDate = value[0];
            let endDate = value[1];
            formattedValue = this.isValidDate(startDate) ? this.formatDateTime(startDate) : '';

            if (endDate) {
              formattedValue += this.isValidDate(endDate) ? ' - ' + this.formatDateTime(endDate) : '';
            }
          }
        }
      } catch (err) {
        formattedValue = value;
      }
    }

    this.inputElement.value = formattedValue;
  }

  formatDateTime(date) {
    let formattedValue = null;

    if (date) {
      if (this.props.timeOnly) {
        formattedValue = this.formatTime(date);
      } else {
        formattedValue = this.formatDate(date, this.props.dateFormat);

        if (this.props.showTime) {
          formattedValue += ' ' + this.formatTime(date);
        }
      }
    }

    return formattedValue;
  }

  formatDate(date, format) {
    if (!date) {
      return '';
    }

    let iFormat;

    const lookAhead = match => {
      const matches = iFormat + 1 < format.length && format.charAt(iFormat + 1) === match;

      if (matches) {
        iFormat++;
      }

      return matches;
    },
          formatNumber = (match, value, len) => {
      let num = '' + value;

      if (lookAhead(match)) {
        while (num.length < len) {
          num = '0' + num;
        }
      }

      return num;
    },
          formatName = (match, value, shortNames, longNames) => {
      return lookAhead(match) ? longNames[value] : shortNames[value];
    };

    let output = '';
    let literal = false;

    if (date) {
      for (iFormat = 0; iFormat < format.length; iFormat++) {
        if (literal) {
          if (format.charAt(iFormat) === '\'' && !lookAhead('\'')) {
            literal = false;
          } else {
            output += format.charAt(iFormat);
          }
        } else {
          switch (format.charAt(iFormat)) {
            case 'd':
              output += formatNumber('d', date.getDate(), 2);
              break;

            case 'D':
              output += formatName('D', date.getDay(), this.props.locale.dayNamesShort, this.props.locale.dayNames);
              break;

            case 'o':
              output += formatNumber('o', Math.round((new Date(date.getFullYear(), date.getMonth(), date.getDate()).getTime() - new Date(date.getFullYear(), 0, 0).getTime()) / 86400000), 3);
              break;

            case 'm':
              output += formatNumber('m', date.getMonth() + 1, 2);
              break;

            case 'M':
              output += formatName('M', date.getMonth(), this.props.locale.monthNamesShort, this.props.locale.monthNames);
              break;

            case 'y':
              output += lookAhead('y') ? date.getFullYear() : (date.getFullYear() % 100 < 10 ? '0' : '') + date.getFullYear() % 100;
              break;

            case '@':
              output += date.getTime();
              break;

            case '!':
              output += date.getTime() * 10000 + this.ticksTo1970;
              break;

            case '\'':
              if (lookAhead('\'')) {
                output += '\'';
              } else {
                literal = true;
              }

              break;

            default:
              output += format.charAt(iFormat);
          }
        }
      }
    }

    return output;
  }

  formatTime(date) {
    if (!date) {
      return '';
    }

    let output = '';
    let hours = date.getHours();
    let minutes = date.getMinutes();
    let seconds = date.getSeconds();
    let milliseconds = date.getMilliseconds();

    if (this.props.hourFormat === '12' && hours > 11 && hours !== 12) {
      hours -= 12;
    }

    if (this.props.hourFormat === '12') {
      output += hours === 0 ? 12 : hours < 10 ? '0' + hours : hours;
    } else {
      output += hours < 10 ? '0' + hours : hours;
    }

    output += ':';
    output += minutes < 10 ? '0' + minutes : minutes;

    if (this.props.showSeconds) {
      output += ':';
      output += seconds < 10 ? '0' + seconds : seconds;
    }

    if (this.props.showMillisec) {
      output += '.';
      output += milliseconds < 100 ? (milliseconds < 10 ? '00' : '0') + milliseconds : milliseconds;
    }

    if (this.props.hourFormat === '12') {
      output += date.getHours() > 11 ? ' PM' : ' AM';
    }

    return output;
  }

  parseValueFromString(text) {
    if (!text || text.trim().length === 0) {
      return null;
    }

    let value;

    if (this.isSingleSelection()) {
      value = this.parseDateTime(text);
    } else if (this.isMultipleSelection()) {
      let tokens = text.split(',');
      value = [];

      for (let token of tokens) {
        value.push(this.parseDateTime(token.trim()));
      }
    } else if (this.isRangeSelection()) {
      let tokens = text.split(' - ');
      value = [];

      for (let i = 0; i < tokens.length; i++) {
        value[i] = this.parseDateTime(tokens[i].trim());
      }
    }

    return value;
  }

  parseDateTime(text) {
    let date;
    let parts = text.split(' ');

    if (this.props.timeOnly) {
      date = new Date();
      this.populateTime(date, parts[0], parts[1]);
    } else {
      if (this.props.showTime) {
        date = this.parseDate(parts[0], this.props.dateFormat);
        this.populateTime(date, parts[1], parts[2]);
      } else {
        date = this.parseDate(text, this.props.dateFormat);
      }
    }

    return date;
  }

  populateTime(value, timeString, ampm) {
    if (this.props.hourFormat === '12' && ampm !== 'PM' && ampm !== 'AM') {
      throw new Error('Invalid Time');
    }

    let time = this.parseTime(timeString, ampm);
    value.setHours(time.hour);
    value.setMinutes(time.minute);
    value.setSeconds(time.second);
    value.setMilliseconds(time.millisecond);
  }

  parseTime(value, ampm) {
    value = this.props.showMillisec ? value.replace('.', ':') : value;
    let tokens = value.split(':');
    let validTokenLength = this.props.showSeconds ? 3 : 2;
    validTokenLength = this.props.showMillisec ? validTokenLength + 1 : validTokenLength;

    if (tokens.length !== validTokenLength || tokens[0].length !== 2 || tokens[1].length !== 2 || this.props.showSeconds && tokens[2].length !== 2 || this.props.showMillisec && tokens[3].length !== 3) {
      throw new Error('Invalid time');
    }

    let h = parseInt(tokens[0], 10);
    let m = parseInt(tokens[1], 10);
    let s = this.props.showSeconds ? parseInt(tokens[2], 10) : null;
    let ms = this.props.showMillisec ? parseInt(tokens[3], 10) : null;

    if (isNaN(h) || isNaN(m) || h > 23 || m > 59 || this.props.hourFormat === '12' && h > 12 || this.props.showSeconds && (isNaN(s) || s > 59) || this.props.showMillisec && (isNaN(s) || s > 1000)) {
      throw new Error('Invalid time');
    } else {
      if (this.props.hourFormat === '12' && h !== 12 && ampm === 'PM') {
        h += 12;
      }

      return {
        hour: h,
        minute: m,
        second: s,
        millisecond: ms
      };
    }
  } // Ported from jquery-ui datepicker parseDate


  parseDate(value, format) {
    if (format == null || value == null) {
      throw new Error('Invalid arguments');
    }

    value = typeof value === "object" ? value.toString() : value + "";

    if (value === "") {
      return null;
    }

    let iFormat,
        dim,
        extra,
        iValue = 0,
        shortYearCutoff = typeof this.props.shortYearCutoff !== "string" ? this.props.shortYearCutoff : new Date().getFullYear() % 100 + parseInt(this.props.shortYearCutoff, 10),
        year = -1,
        month = -1,
        day = -1,
        doy = -1,
        literal = false,
        date,
        lookAhead = match => {
      let matches = iFormat + 1 < format.length && format.charAt(iFormat + 1) === match;

      if (matches) {
        iFormat++;
      }

      return matches;
    },
        getNumber = match => {
      let isDoubled = lookAhead(match),
          size = match === "@" ? 14 : match === "!" ? 20 : match === "y" && isDoubled ? 4 : match === "o" ? 3 : 2,
          minSize = match === "y" ? size : 1,
          digits = new RegExp("^\\d{" + minSize + "," + size + "}"),
          num = value.substring(iValue).match(digits);

      if (!num) {
        throw new Error('Missing number at position ' + iValue);
      }

      iValue += num[0].length;
      return parseInt(num[0], 10);
    },
        getName = (match, shortNames, longNames) => {
      let index = -1;
      let arr = lookAhead(match) ? longNames : shortNames;
      let names = [];

      for (let i = 0; i < arr.length; i++) {
        names.push([i, arr[i]]);
      }

      names.sort((a, b) => {
        return -(a[1].length - b[1].length);
      });

      for (let i = 0; i < names.length; i++) {
        let name = names[i][1];

        if (value.substr(iValue, name.length).toLowerCase() === name.toLowerCase()) {
          index = names[i][0];
          iValue += name.length;
          break;
        }
      }

      if (index !== -1) {
        return index + 1;
      } else {
        throw new Error('Unknown name at position ' + iValue);
      }
    },
        checkLiteral = () => {
      if (value.charAt(iValue) !== format.charAt(iFormat)) {
        throw new Error('Unexpected literal at position ' + iValue);
      }

      iValue++;
    };

    if (this.props.view === 'month') {
      day = 1;
    }

    for (iFormat = 0; iFormat < format.length; iFormat++) {
      if (literal) {
        if (format.charAt(iFormat) === "'" && !lookAhead("'")) {
          literal = false;
        } else {
          checkLiteral();
        }
      } else {
        switch (format.charAt(iFormat)) {
          case "d":
            day = getNumber("d");
            break;

          case "D":
            getName("D", this.props.locale.dayNamesShort, this.props.locale.dayNames);
            break;

          case "o":
            doy = getNumber("o");
            break;

          case "m":
            month = getNumber("m");
            break;

          case "M":
            month = getName("M", this.props.locale.monthNamesShort, this.props.locale.monthNames);
            break;

          case "y":
            year = getNumber("y");
            break;

          case "@":
            date = new Date(getNumber("@"));
            year = date.getFullYear();
            month = date.getMonth() + 1;
            day = date.getDate();
            break;

          case "!":
            date = new Date((getNumber("!") - this.ticksTo1970) / 10000);
            year = date.getFullYear();
            month = date.getMonth() + 1;
            day = date.getDate();
            break;

          case "'":
            if (lookAhead("'")) {
              checkLiteral();
            } else {
              literal = true;
            }

            break;

          default:
            checkLiteral();
        }
      }
    }

    if (iValue < value.length) {
      extra = value.substr(iValue);

      if (!/^\s+/.test(extra)) {
        throw new Error('Extra/unparsed characters found in date: ' + extra);
      }
    }

    if (year === -1) {
      year = new Date().getFullYear();
    } else if (year < 100) {
      year += new Date().getFullYear() - new Date().getFullYear() % 100 + (year <= shortYearCutoff ? 0 : -100);
    }

    if (doy > -1) {
      month = 1;
      day = doy;

      do {
        dim = this.getDaysCountInMonth(year, month - 1);

        if (day <= dim) {
          break;
        }

        month++;
        day -= dim;
      } while (true);
    }

    date = this.daylightSavingAdjust(new Date(year, month - 1, day));

    if (date.getFullYear() !== year || date.getMonth() + 1 !== month || date.getDate() !== day) {
      throw new Error('Invalid date'); // E.g. 31/02/00
    }

    return date;
  }

  renderBackwardNavigator() {
    return _("button", {
      type: "button",
      className: "p-datepicker-prev p-link",
      onClick: this.onPrevButtonClick,
      onKeyDown: e => this.onContainerButtonKeydown(e)
    }, _("span", {
      className: "p-datepicker-prev-icon pi pi-chevron-left"
    }));
  }

  renderForwardNavigator() {
    return _("button", {
      type: "button",
      className: "p-datepicker-next p-link",
      onClick: this.onNextButtonClick,
      onKeyDown: e => this.onContainerButtonKeydown(e)
    }, _("span", {
      className: "p-datepicker-next-icon pi pi-chevron-right"
    }));
  }

  isInMinYear(viewDate) {
    return this.props.minDate && this.props.minDate.getFullYear() === viewDate.getFullYear();
  }

  isInMaxYear(viewDate) {
    return this.props.maxDate && this.props.maxDate.getFullYear() === viewDate.getFullYear();
  }

  renderTitleMonthElement(month) {
    if (this.props.monthNavigator && this.props.view !== 'month') {
      let viewDate = this.getViewDate();
      let viewMonth = viewDate.getMonth();
      return _("select", {
        className: "p-datepicker-month",
        onChange: this.onMonthDropdownChange,
        value: viewMonth
      }, this.props.locale.monthNames.map((month, index) => {
        if ((!this.isInMinYear(viewDate) || index >= this.props.minDate.getMonth()) && (!this.isInMaxYear(viewDate) || index <= this.props.maxDate.getMonth())) {
          return _("option", {
            key: month,
            value: index
          }, month);
        }

        return null;
      }));
    } else {
      return _("span", {
        className: "p-datepicker-month"
      }, this.props.locale.monthNames[month]);
    }
  }

  renderTitleYearElement(year) {
    if (this.props.yearNavigator) {
      let yearOptions = [];
      const years = this.props.yearRange.split(':');
      const yearStart = parseInt(years[0], 10);
      const yearEnd = parseInt(years[1], 10);

      for (let i = yearStart; i <= yearEnd; i++) {
        yearOptions.push(i);
      }

      let viewDate = this.getViewDate();
      let viewYear = viewDate.getFullYear();
      return _("select", {
        className: "p-datepicker-year",
        onChange: this.onYearDropdownChange,
        value: viewYear
      }, yearOptions.map(year => {
        if (!(this.props.minDate && this.props.minDate.getFullYear() > year) && !(this.props.maxDate && this.props.maxDate.getFullYear() < year)) {
          return _("option", {
            key: year,
            value: year
          }, year);
        }

        return null;
      }));
    } else {
      return _("span", {
        className: "p-datepicker-year"
      }, year);
    }
  }

  renderTitle(monthMetaData) {
    const month = this.renderTitleMonthElement(monthMetaData.month);
    const year = this.renderTitleYearElement(monthMetaData.year);
    return _("div", {
      className: "p-datepicker-title"
    }, month, year);
  }

  renderDayNames(weekDays) {
    const dayNames = weekDays.map(weekDay => _("th", {
      key: weekDay,
      scope: "col"
    }, _("span", null, weekDay)));

    if (this.props.showWeek) {
      const weekHeader = _("th", {
        scope: "col",
        key: 'wn',
        className: "p-datepicker-weekheader p-disabled"
      }, _("span", null, this.props.locale['weekHeader']));
      return [weekHeader, ...dayNames];
    } else {
      return dayNames;
    }
  }

  renderDateCellContent(date, className, groupIndex) {
    const content = this.props.dateTemplate ? this.props.dateTemplate(date) : date.day;
    return _("span", {
      className: className,
      onClick: e => this.onDateSelect(e, date),
      onKeyDown: e => this.onDateCellKeydown(e, date, groupIndex)
    }, content);
  }

  renderWeek(weekDates, weekNumber, groupIndex) {
    const week = weekDates.map(date => {
      const selected = this.isSelected(date);
      const cellClassName = classNames({
        'p-datepicker-other-month': date.otherMonth,
        'p-datepicker-today': date.today
      });
      const dateClassName = classNames({
        'p-highlight': selected,
        'p-disabled': !date.selectable
      });
      const content = date.otherMonth && !this.props.showOtherMonths ? null : this.renderDateCellContent(date, dateClassName, groupIndex);
      return _("td", {
        key: date.day,
        className: cellClassName
      }, content);
    });

    if (this.props.showWeek) {
      const weekNumberCell = _("td", {
        key: 'wn' + weekNumber,
        className: "p-datepicker-weeknumber"
      }, _("span", {
        className: "p-disabled"
      }, weekNumber));
      return [weekNumberCell, ...week];
    } else {
      return week;
    }
  }

  renderDates(monthMetaData, groupIndex) {
    return monthMetaData.dates.map((weekDates, index) => {
      return _("tr", {
        key: index
      }, this.renderWeek(weekDates, monthMetaData.weekNumbers[index], groupIndex));
    });
  }

  renderDateViewGrid(monthMetaData, weekDays, groupIndex) {
    const dayNames = this.renderDayNames(weekDays);
    const dates = this.renderDates(monthMetaData, groupIndex);
    return _("div", {
      className: "p-datepicker-calendar-container"
    }, _("table", {
      className: "p-datepicker-calendar"
    }, _("thead", null, _("tr", null, dayNames)), _("tbody", null, dates)));
  }

  renderMonth(monthMetaData, index) {
    const weekDays = this.createWeekDays();
    const backwardNavigator = index === 0 ? this.renderBackwardNavigator() : null;
    const forwardNavigator = this.props.numberOfMonths === 1 || index === this.props.numberOfMonths - 1 ? this.renderForwardNavigator() : null;
    const title = this.renderTitle(monthMetaData);
    const dateViewGrid = this.renderDateViewGrid(monthMetaData, weekDays, index);
    const header = this.props.headerTemplate ? this.props.headerTemplate() : null;
    return _("div", {
      key: monthMetaData.month,
      className: "p-datepicker-group"
    }, _("div", {
      className: "p-datepicker-header"
    }, header, backwardNavigator, forwardNavigator, title), dateViewGrid);
  }

  renderMonths(monthsMetaData) {
    return monthsMetaData.map((monthMetaData, index) => {
      return this.renderMonth(monthMetaData, index);
    });
  }

  renderDateView() {
    let viewDate = this.getViewDate();
    const monthsMetaData = this.createMonths(viewDate.getMonth(), viewDate.getFullYear());
    const months = this.renderMonths(monthsMetaData);
    return _(React.Fragment, null, months);
  }

  renderMonthViewMonth(index) {
    const className = classNames('p-monthpicker-month', {
      'p-highlight': this.isMonthSelected(index)
    });
    const monthName = this.props.locale.monthNamesShort[index];
    return _("span", {
      key: monthName,
      className: className,
      onClick: event => this.onMonthSelect(event, index),
      onKeyDown: event => this.onMonthCellKeydown(event, index)
    }, monthName);
  }

  renderMonthViewMonths() {
    let months = [];

    for (let i = 0; i <= 11; i++) {
      months.push(this.renderMonthViewMonth(i));
    }

    return months;
  }

  renderMonthView() {
    const backwardNavigator = this.renderBackwardNavigator();
    const forwardNavigator = this.renderForwardNavigator();
    const yearElement = this.renderTitleYearElement(this.getViewDate().getFullYear());
    const months = this.renderMonthViewMonths();
    return _(React.Fragment, null, _("div", {
      className: "p-datepicker-header"
    }, backwardNavigator, forwardNavigator, _("div", {
      className: "p-datepicker-title"
    }, yearElement)), _("div", {
      className: "p-monthpicker"
    }, months));
  }

  renderDatePicker() {
    if (!this.props.timeOnly) {
      if (this.props.view === 'date') {
        return this.renderDateView();
      } else if (this.props.view === 'month') {
        return this.renderMonthView();
      } else {
        return null;
      }
    }
  }

  renderHourPicker() {
    let currentTime = this.props.value && this.props.value instanceof Date ? this.props.value : this.getViewDate();
    let hour = currentTime.getHours();

    if (this.props.hourFormat === '12') {
      if (hour === 0) hour = 12;else if (hour > 11 && hour !== 12) hour = hour - 12;
    }

    const hourDisplay = hour < 10 ? '0' + hour : hour;
    return _("div", {
      className: "p-hour-picker"
    }, _("button", {
      type: "button",
      className: "p-link",
      onMouseDown: e => this.onTimePickerElementMouseDown(e, 0, 1),
      onMouseUp: this.onTimePickerElementMouseUp,
      onMouseLeave: this.onTimePickerElementMouseLeave,
      onKeyDown: e => this.onContainerButtonKeydown(e)
    }, _("span", {
      className: "pi pi-chevron-up"
    })), _("span", null, hourDisplay), _("button", {
      type: "button",
      className: "p-link",
      onMouseDown: e => this.onTimePickerElementMouseDown(e, 0, -1),
      onMouseUp: this.onTimePickerElementMouseUp,
      onMouseLeave: this.onTimePickerElementMouseLeave,
      onKeyDown: e => this.onContainerButtonKeydown(e)
    }, _("span", {
      className: "pi pi-chevron-down"
    })));
  }

  renderMinutePicker() {
    let currentTime = this.props.value && this.props.value instanceof Date ? this.props.value : this.getViewDate();
    let minute = currentTime.getMinutes();
    let minuteDisplay = minute < 10 ? '0' + minute : minute;
    return _("div", {
      className: "p-minute-picker"
    }, _("button", {
      type: "button",
      className: "p-link",
      onMouseDown: e => this.onTimePickerElementMouseDown(e, 1, 1),
      onMouseUp: this.onTimePickerElementMouseUp,
      onMouseLeave: this.onTimePickerElementMouseLeave,
      onKeyDown: e => this.onContainerButtonKeydown(e)
    }, _("span", {
      className: "pi pi-chevron-up"
    })), _("span", null, minuteDisplay), _("button", {
      type: "button",
      className: "p-link",
      onMouseDown: e => this.onTimePickerElementMouseDown(e, 1, -1),
      onMouseUp: this.onTimePickerElementMouseUp,
      onMouseLeave: this.onTimePickerElementMouseLeave,
      onKeyDown: e => this.onContainerButtonKeydown(e)
    }, _("span", {
      className: "pi pi-chevron-down"
    })));
  }

  renderSecondPicker() {
    if (this.props.showSeconds) {
      let currentTime = this.props.value && this.props.value instanceof Date ? this.props.value : this.getViewDate();
      let second = currentTime.getSeconds();
      let secondDisplay = second < 10 ? '0' + second : second;
      return _("div", {
        className: "p-second-picker"
      }, _("button", {
        type: "button",
        className: "p-link",
        onMouseDown: e => this.onTimePickerElementMouseDown(e, 2, 1),
        onMouseUp: this.onTimePickerElementMouseUp,
        onMouseLeave: this.onTimePickerElementMouseLeave,
        onKeyDown: e => this.onContainerButtonKeydown(e)
      }, _("span", {
        className: "pi pi-chevron-up"
      })), _("span", null, secondDisplay), _("button", {
        type: "button",
        className: "p-link",
        onMouseDown: e => this.onTimePickerElementMouseDown(e, 2, -1),
        onMouseUp: this.onTimePickerElementMouseUp,
        onMouseLeave: this.onTimePickerElementMouseLeave,
        onKeyDown: e => this.onContainerButtonKeydown(e)
      }, _("span", {
        className: "pi pi-chevron-down"
      })));
    }

    return null;
  }

  renderMiliSecondPicker() {
    if (this.props.showMillisec) {
      let currentTime = this.props.value && this.props.value instanceof Date ? this.props.value : this.getViewDate();
      let millisecond = currentTime.getMilliseconds();
      let millisecondDisplay = millisecond < 100 ? (millisecond < 10 ? '00' : '0') + millisecond : millisecond;
      return _("div", {
        className: "p-millisecond-picker"
      }, _("button", {
        type: "button",
        className: "p-link",
        onMouseDown: e => this.onTimePickerElementMouseDown(e, 3, 1),
        onMouseUp: this.onTimePickerElementMouseUp,
        onMouseLeave: this.onTimePickerElementMouseLeave,
        onKeyDown: e => this.onContainerButtonKeydown(e)
      }, _("span", {
        className: "pi pi-chevron-up"
      })), _("span", null, millisecondDisplay), _("button", {
        type: "button",
        className: "p-link",
        onMouseDown: e => this.onTimePickerElementMouseDown(e, 3, -1),
        onMouseUp: this.onTimePickerElementMouseUp,
        onMouseLeave: this.onTimePickerElementMouseLeave,
        onKeyDown: e => this.onContainerButtonKeydown(e)
      }, _("span", {
        className: "pi pi-chevron-down"
      })));
    }

    return null;
  }

  renderAmPmPicker() {
    if (this.props.hourFormat === '12') {
      let currentTime = this.props.value && this.props.value instanceof Date ? this.props.value : this.getViewDate();
      let hour = currentTime.getHours();
      let display = hour > 11 ? 'PM' : 'AM';
      return _("div", {
        className: "p-ampm-picker"
      }, _("button", {
        type: "button",
        className: "p-link",
        onClick: this.toggleAmPm
      }, _("span", {
        className: "pi pi-chevron-up"
      })), _("span", null, display), _("button", {
        type: "button",
        className: "p-link",
        onClick: this.toggleAmPm
      }, _("span", {
        className: "pi pi-chevron-down"
      })));
    } else {
      return null;
    }
  }

  renderSeparator(separator) {
    return _("div", {
      className: "p-separator"
    }, _("span", {
      className: "p-separator-spacer"
    }, _("span", {
      className: "pi pi-chevron-up"
    })), _("span", null, separator), _("span", {
      className: "p-separator-spacer"
    }, _("span", {
      className: "pi pi-chevron-down"
    })));
  }

  renderTimePicker() {
    if (this.props.showTime || this.props.timeOnly) {
      return _("div", {
        className: "p-timepicker"
      }, this.renderHourPicker(), this.renderSeparator(':'), this.renderMinutePicker(), this.props.showSeconds && this.renderSeparator(':'), this.renderSecondPicker(), this.props.showMillisec && this.renderSeparator('.'), this.renderMiliSecondPicker(), this.props.hourFormat === '12' && this.renderSeparator(':'), this.renderAmPmPicker());
    }

    return null;
  }

  renderInputElement() {
    if (!this.props.inline) {
      const className = classNames('p-inputtext p-component', this.props.inputClassName);
      return _(InputText, {
        ref: el => this.inputElement = ReactDOM.findDOMNode(el),
        id: this.props.inputId,
        name: this.props.name,
        type: "text",
        className: className,
        style: this.props.inputStyle,
        readOnly: this.props.readOnlyInput,
        disabled: this.props.disabled,
        required: this.props.required,
        autoComplete: "off",
        placeholder: this.props.placeholder,
        onInput: this.onUserInput,
        onFocus: this.onInputFocus,
        onBlur: this.onInputBlur,
        onKeyDown: this.onInputKeyDown,
        "aria-labelledby": this.props.ariaLabelledBy
      });
    } else {
      return null;
    }
  }

  renderButton() {
    if (this.props.showIcon) {
      return _(Button, {
        type: "button",
        icon: this.props.icon,
        onClick: this.onButtonClick,
        tabIndex: "-1",
        disabled: this.props.disabled,
        className: "p-datepicker-trigger p-calendar-button"
      });
    } else {
      return null;
    }
  }

  renderButtonBar() {
    if (this.props.showButtonBar) {
      return _("div", {
        className: "p-datepicker-buttonbar"
      }, _(Button, {
        type: "button",
        label: this.props.locale.today,
        onClick: this.onTodayButtonClick,
        onKeyDown: e => this.onContainerButtonKeydown(e),
        className: this.props.todayButtonClassName
      }), _(Button, {
        type: "button",
        label: this.props.locale.clear,
        onClick: this.onClearButtonClick,
        onKeyDown: e => this.onContainerButtonKeydown(e),
        className: this.props.clearButtonClassName
      }));
    } else {
      return null;
    }
  }

  renderFooter() {
    if (this.props.footerTemplate) {
      const content = this.props.footerTemplate();
      return _("div", {
        className: "p-datepicker-footer"
      }, content);
    } else {
      return null;
    }
  }

  render() {
    const className = classNames('p-calendar', this.props.className, {
      'p-calendar-w-btn': this.props.showIcon,
      'p-calendar-timeonly': this.props.timeOnly,
      'p-inputwrapper-filled': this.props.value || DomHandler.hasClass(this.inputElement, 'p-filled') && this.inputElement.value !== ''
    });
    const panelClassName = classNames('p-datepicker p-component', this.props.panelClassName, {
      'p-datepicker-inline': this.props.inline,
      'p-input-overlay': !this.props.inline,
      'p-shadow': !this.props.inline,
      'p-disabled': this.props.disabled,
      'p-datepicker-timeonly': this.props.timeOnly,
      'p-datepicker-multiple-month': this.props.numberOfMonths > 1,
      'p-datepicker-monthpicker': this.props.view === 'month',
      'p-datepicker-touch-ui': this.props.touchUI
    });
    const input = this.renderInputElement();
    const button = this.renderButton();
    const datePicker = this.renderDatePicker();
    const timePicker = this.renderTimePicker();
    const buttonBar = this.renderButtonBar();
    const footer = this.renderFooter();
    return _("span", {
      ref: el => this.container = el,
      id: this.props.id,
      className: className,
      style: this.props.style
    }, input, button, _(CalendarPanel, {
      ref: el => this.panel = ReactDOM.findDOMNode(el),
      className: panelClassName,
      style: this.props.panelStyle,
      appendTo: this.props.appendTo
    }, datePicker, timePicker, buttonBar, footer));
  }

}

Calendar.defaultProps = {
	id: null,
	name: null,
	value: null,
	viewDate: null,
	style: null,
	className: null,
	inline: false,
	selectionMode: 'single',
	inputId: null,
	inputStyle: null,
	inputClassName: null,
	required: false,
	readOnlyInput: false,
	keepInvalid: false,
	disabled: false,
	tabIndex: null,
	placeholder: null,
	showIcon: false,
	icon: 'pi pi-calendar',
	showOnFocus: true,
	numberOfMonths: 1,
	view: 'date',
	touchUI: false,
	showTime: false,
	timeOnly: false,
	showSeconds: false,
	showMillisec: false,
	hourFormat: '24',
	stepHour: 1,
	stepMinute: 1,
	stepSecond: 1,
	stepMillisec: 1,
	shortYearCutoff: '+10',
	hideOnDateTimeSelect: false,
	showWeek: false,
	locale: {
		firstDayOfWeek: 0,
		dayNames: ["Sunday", "Monday", "Tuesday", "Wednesday", "Thursday", "Friday", "Saturday"],
		dayNamesShort: ["Sun", "Mon", "Tue", "Wed", "Thu", "Fri", "Sat"],
		dayNamesMin: ["Su","Mo","Tu","We","Th","Fr","Sa"],
		monthNames: [ "January","February","March","April","May","June","July","August","September","October","November","December" ],
		monthNamesShort: [ "Jan", "Feb", "Mar", "Apr", "May", "Jun","Jul", "Aug", "Sep", "Oct", "Nov", "Dec" ],
		today: 'Today',
		clear: 'Clear',
		weekHeader: 'Wk'
	},
	dateFormat: 'mm/dd/yy',
	panelStyle: null,
	panelClassName: null,
	monthNavigator: false,
	yearNavigator: false,
	disabledDates: null,
	disabledDays: null,
	minDate: null,
	maxDate: null,
	maxDateCount: null,
	showOtherMonths: true,
	selectOtherMonths: false,
	showButtonBar: false,
	todayButtonClassName: 'p-button-secondary',
	clearButtonClassName: 'p-button-secondary',
	autoZIndex: true,
	baseZIndex: 0,
	appendTo: null,
	tooltip: null,
	tooltipOptions: null,
	ariaLabelledBy: null,
	dateTemplate: null,
	headerTemplate: null,
	footerTemplate: null,
	onFocus: null,
	onBlur: null,
	onInput: null,
	onSelect: null,
	onChange: null,
	onViewDateChange: null,
	onTodayButtonClick: null,
	onClearButtonClick: null
}

Calendar.propTypes = {
	id: PropTypes.string,
	name: PropTypes.string,
	value: PropTypes.any,
	viewDate: PropTypes.any,
	style: PropTypes.object,
	className: PropTypes.string,
	inline: PropTypes.bool,
	selectionMode: PropTypes.string,
	inputId: PropTypes.string,
	inputStyle: PropTypes.object,
	inputClassName: PropTypes.string,
	required: PropTypes.bool,
	readOnlyInput: PropTypes.bool,
	keepInvalid: PropTypes.bool,
	disabled: PropTypes.bool,
	tabIndex: PropTypes.string,
	placeholder: PropTypes.string,
	showIcon: PropTypes.bool,
	icon: PropTypes.string,
	showOnFocus: PropTypes.bool,
	numberOfMonths: PropTypes.number,
	view: PropTypes.string,
	touchUI: PropTypes.bool,
	showTime: PropTypes.bool,
	timeOnly: PropTypes.bool,
	showSeconds: PropTypes.bool,
	showMillisec: PropTypes.bool,
	hourFormat: PropTypes.string,
	stepHour: PropTypes.number,
	stepMinute: PropTypes.number,
	stepSecond: PropTypes.number,
	stepMillisec: PropTypes.number,
	shortYearCutoff: PropTypes.string,
	hideOnDateTimeSelect: PropTypes.bool,
	showWeek: PropTypes.bool,
	locale: PropTypes.object,
	dateFormat: PropTypes.string,
	panelStyle: PropTypes.object,
	panelClassName: PropTypes.string,
	monthNavigator: PropTypes.bool,
	yearNavigator: PropTypes.bool,
	disabledDates: PropTypes.array,
	disabledDays: PropTypes.array,
	minDate: PropTypes.any,
	maxDate: PropTypes.any,
	maxDateCount: PropTypes.number,
	showOtherMonths: PropTypes.bool,
	selectOtherMonths: PropTypes.bool,
	showButtonBar: PropTypes.bool,
	todayButtonClassName: PropTypes.string,
	clearButtonClassName: PropTypes.string,
	autoZIndex: PropTypes.bool,
	baseZIndex: PropTypes.number,
	appendTo: PropTypes.any,
	tooltip: PropTypes.string,
	tooltipOptions: PropTypes.object,
	ariaLabelledBy: PropTypes.string,
	dateTemplate: PropTypes.func,
	headerTemplate: PropTypes.func,
	footerTemplate: PropTypes.func,
	onFocus: PropTypes.func,
	onBlur: PropTypes.func,
	onInput: PropTypes.func,
	onSelect: PropTypes.func,
	onChange: PropTypes.func,
	onViewDateChange: PropTypes.func,
	onTodayButtonClick: PropTypes.func,
	onClearButtonClick: PropTypes.func,
}

class CalendarPanel extends React.Component {
  renderElement() {
    return _("div", {
      ref: el => this.element = el,
      className: this.props.className,
      style: this.props.style
    }, this.props.children);
  }

  render() {
    let element = this.renderElement();
    if (this.props.appendTo) return ReactDOM.createPortal(element, this.props.appendTo);else return element;
  }

}

CalendarPanel.defaultProps = {
	appendTo: null,
	style: null,
	className: null
};

CalendarPanel.propTypes = {
	appendTo: PropTypes.object,
	style: PropTypes.object,
	className: PropTypes.string
};




class Card extends React.Component {
  renderHeader() {
    return _("div", {
      className: "p-card-header"
    }, this.props.header);
  }

  renderBody() {
    let title, subTitle, footer, children;

    if (this.props.title) {
      title = _("div", {
        className: "p-card-title"
      }, this.props.title);
    }

    if (this.props.subTitle) {
      subTitle = _("div", {
        className: "p-card-subtitle"
      }, this.props.subTitle);
    }

    if (this.props.footer) {
      footer = _("div", {
        className: "p-card-footer"
      }, " ", this.props.footer);
    }

    if (this.props.children) {
      children = _("div", {
        className: "p-card-content"
      }, " ", this.props.children, " ");
    }

    return _("div", {
      className: "p-card-body"
    }, title, subTitle, children, footer);
  }

  render() {
    let header, body;
    let className = classNames('p-card p-component', this.props.className);

    if (this.props.header) {
      header = this.renderHeader();
    }

    body = this.renderBody();
    return _("div", {
      className: className,
      style: this.props.style
    }, header, body);
  }

}

Card.defaultProps = {
	id: null,
	header: null,
	footer: null,
	title: null,
	subTitle: null,
	style: null,
	className: null
};

Card.propTypes = {
	id: PropTypes.string,
	header: PropTypes.any,
	footer: PropTypes.any,
	title: PropTypes.string,
	subTitle: PropTypes.string,
	style: PropTypes.object,
	className: PropTypes.string
};


class Checkbox extends React.Component {
  constructor(props) {
    super(props);
    this.state = {};
    this.onClick = this.onClick.bind(this);
    this.onFocus = this.onFocus.bind(this);
    this.onBlur = this.onBlur.bind(this);
    this.onKeyDown = this.onKeyDown.bind(this);
  }

  onClick(e) {
    if (!this.props.disabled && !this.props.readOnly && this.props.onChange) {
      this.props.onChange({
        originalEvent: e,
        value: this.props.value,
        checked: !this.props.checked,
        stopPropagation: () => {},
        preventDefault: () => {},
        target: {
          type: 'checkbox',
          name: this.props.name,
          id: this.props.id,
          value: this.props.value,
          checked: !this.props.checked
        }
      });
      this.input.checked = !this.props.checked;
      this.input.focus();
      e.preventDefault();
    }
  }

  componentDidMount() {
    if (this.props.tooltip) {
      this.renderTooltip();
    }
  }

  componentWillUnmount() {
    if (this.tooltip) {
      this.tooltip.destroy();
      this.tooltip = null;
    }
  }

  componentDidUpdate(prevProps) {
    this.input.checked = this.props.checked;

    if (prevProps.tooltip !== this.props.tooltip) {
      if (this.tooltip) this.tooltip.updateContent(this.props.tooltip);else this.renderTooltip();
    }
  }

  onFocus() {
    this.setState({
      focused: true
    });
  }

  onBlur() {
    this.setState({
      focused: false
    });
  }

  onKeyDown(event) {
    if (event.key === 'Enter') {
      this.onClick(event);
      event.preventDefault();
    }
  }

  renderTooltip() {
    this.tooltip = new Tooltip({
      target: this.element,
      content: this.props.tooltip,
      options: this.props.tooltipOptions
    });
  }

  render() {
    let containerClass = classNames('p-checkbox p-component', this.props.className);
    let boxClass = classNames('p-checkbox-box p-component', {
      'p-highlight': this.props.checked,
      'p-disabled': this.props.disabled,
      'p-focus': this.state.focused
    });
    let iconClass = classNames('p-checkbox-icon p-c', {
      'pi pi-check': this.props.checked
    });
    return _("div", {
      ref: el => this.element = el,
      id: this.props.id,
      className: containerClass,
      style: this.props.style,
      onClick: this.onClick,
      onContextMenu: this.props.onContextMenu,
      onMouseDown: this.props.onMouseDown
    }, _("div", {
      className: "p-hidden-accessible"
    }, _("input", {
      type: "checkbox",
      "aria-labelledby": this.props.ariaLabelledBy,
      ref: el => this.input = el,
      id: this.props.inputId,
      name: this.props.name,
      defaultChecked: this.props.checked,
      onKeyDown: this.onKeyDown,
      onFocus: this.onFocus,
      onBlur: this.onBlur,
      disabled: this.props.disabled,
      readOnly: this.props.readOnly,
      required: this.props.required
    })), _("div", {
      className: boxClass,
      ref: el => this.box = el,
      role: "checkbox",
      "aria-checked": this.props.checked
    }, _("span", {
      className: iconClass
    })));
  }

}

Checkbox.defaultProps = {
	id: null,
	inputId: null,
	value: null,
	name: null,
	checked: false,
	style: null,
	className: null,
	disabled: false,
	required: false,
	readOnly: false,
	tooltip: null,
	tooltipOptions: null,
	ariaLabelledBy: null,
	onChange: null,
	onMouseDown: null,
	onContextMenu: null
};

Checkbox.propTypes = {
	id: PropTypes.string,
	inputId: PropTypes.string,
	value: PropTypes.any,
	name: PropTypes.string,
	checked: PropTypes.bool,
	style: PropTypes.object,
	className: PropTypes.string,
	disabled: PropTypes.bool,
	required: PropTypes.bool,
	readOnly: PropTypes.bool,
	tooltip: PropTypes.string,
	tooltipOptions: PropTypes.object,
	ariaLabelledBy: PropTypes.string,
	onChange: PropTypes.func,
	onMouseDown: PropTypes.func,
	onContextMenu: PropTypes.func
};

class Dropdown extends React.Component {
  constructor(props) {
    super(props);
    this.state = {
      filter: '',
      overlayVisible: null
    };
    this.onClick = this.onClick.bind(this);
    this.onInputFocus = this.onInputFocus.bind(this);
    this.onInputBlur = this.onInputBlur.bind(this);
    this.onInputKeyDown = this.onInputKeyDown.bind(this);
    this.onEditableInputClick = this.onEditableInputClick.bind(this);
    this.onEditableInputChange = this.onEditableInputChange.bind(this);
    this.onEditableInputFocus = this.onEditableInputFocus.bind(this);
    this.onOptionClick = this.onOptionClick.bind(this);
    this.onFilterInputChange = this.onFilterInputChange.bind(this);
    this.onFilterInputKeyDown = this.onFilterInputKeyDown.bind(this);
    this.onPanelClick = this.onPanelClick.bind(this);
    this.clear = this.clear.bind(this);
  }

  onClick(event) {
    if (this.props.disabled) {
      return;
    }

    if (!this.isClearClicked(event)) {
      this.focusInput.focus();

      if (this.panel.element.offsetParent) {
        this.hide();
      } else {
        this.show();

        if (this.props.filter && this.props.filterInputAutoFocus) {
          setTimeout(() => {
            this.filterInput.focus();
          }, 200);
        }
      }
    }
  }

  onInputFocus(event) {
    DomHandler.addClass(this.container, 'p-focus');
    this.focus = true;

    if (this.props.onFocus) {
      this.props.onFocus(event);
    }
  }

  onInputBlur(event) {
    DomHandler.removeClass(this.container, 'p-focus');
    this.focus = false;

    if (this.props.onBlur) {
      this.props.onBlur(event);
    }
  }

  onPanelClick(event) {
    event.stopPropagation();
  }

  onUpKey(event) {
    if (this.props.options) {
      let selectedItemIndex = this.findOptionIndex(this.props.value);
      let prevItem = this.findPrevVisibleItem(selectedItemIndex);

      if (prevItem) {
        this.selectItem({
          originalEvent: event,
          option: prevItem
        });
      }
    }

    event.preventDefault();
  }

  onDownKey(event) {
    if (this.props.options) {
      if (!this.panel.element.offsetParent && event.altKey) {
        this.show();
      } else {
        let selectedItemIndex = this.findOptionIndex(this.props.value);
        let nextItem = this.findNextVisibleItem(selectedItemIndex);

        if (nextItem) {
          this.selectItem({
            originalEvent: event,
            option: nextItem
          });
        }
      }
    }

    event.preventDefault();
  }

  onInputKeyDown(event) {
    switch (event.which) {
      //down
      case 40:
        this.onDownKey(event);
        break;
      //up

      case 38:
        this.onUpKey(event);
        break;
      //space

      case 32:
        if (!this.panel.element.offsetParent) {
          this.show();
          event.preventDefault();
        }

        break;
      //enter

      case 13:
        this.hide();
        event.preventDefault();
        break;
      //escape and tab

      case 27:
      case 9:
        this.hide();
        break;

      default:
        this.search(event);
        break;
    }
  }

  search(event) {
    if (this.searchTimeout) {
      clearTimeout(this.searchTimeout);
    }

    const char = String.fromCharCode(event.keyCode);
    this.previousSearchChar = this.currentSearchChar;
    this.currentSearchChar = char;
    if (this.previousSearchChar === this.currentSearchChar) this.searchValue = this.currentSearchChar;else this.searchValue = this.searchValue ? this.searchValue + char : char;
    let searchIndex = this.props.value ? this.findOptionIndex(this.props.value) : -1;
    let newOption = this.searchOption(++searchIndex);

    if (newOption) {
      this.selectItem({
        originalEvent: event,
        option: newOption
      });
      this.selectedOptionUpdated = true;
    }

    this.searchTimeout = setTimeout(() => {
      this.searchValue = null;
    }, 250);
  }

  searchOption(index) {
    let option;

    if (this.searchValue) {
      option = this.searchOptionInRange(index, this.props.options.length);

      if (!option) {
        option = this.searchOptionInRange(0, index);
      }
    }

    return option;
  }

  searchOptionInRange(start, end) {
    for (let i = start; i < end; i++) {
      let opt = this.props.options[i];
      let label = this.getOptionLabel(opt).toString().toLocaleLowerCase(this.props.filterLocale);

      if (label.startsWith(this.searchValue.toLocaleLowerCase(this.props.filterLocale))) {
        return opt;
      }
    }

    return null;
  }

  filter(options) {
    let filterValue = this.state.filter.trim().toLocaleLowerCase(this.props.filterLocale);
    let searchFields = this.props.filterBy ? this.props.filterBy.split(',') : [this.props.optionLabel || 'label'];
    let items = FilterUtils.filter(options, searchFields, filterValue, this.props.filterMatchMode, this.props.filterLocale);
    return items && items.length ? items : null;
  }

  findNextVisibleItem(index) {
    let i = index + 1;

    if (i === this.props.options.length) {
      return null;
    }

    let option = this.props.options[i];

    if (option.disabled) {
      return this.findNextVisibleItem(i);
    }

    if (this.hasFilter()) {
      if (this.filter([option])) return option;else return this.findNextVisibleItem(i);
    } else {
      return option;
    }
  }

  findPrevVisibleItem(index) {
    let i = index - 1;

    if (i === -1) {
      return null;
    }

    let option = this.props.options[i];

    if (option.disabled) {
      return this.findPrevVisibleItem(i);
    }

    if (this.hasFilter()) {
      if (this.filter([option])) return option;else return this.findPrevVisibleItem(i);
    } else {
      return option;
    }
  }

  onEditableInputClick(event) {
    this.bindDocumentClickListener();
    event.stopPropagation();
  }

  onEditableInputChange(event) {
    this.props.onChange({
      originalEvent: event.originalEvent,
      value: event.target.value,
      stopPropagation: () => {},
      preventDefault: () => {},
      target: {
        name: this.props.name,
        id: this.props.id,
        value: event.target.value
      }
    });
  }

  onEditableInputFocus(event) {
    DomHandler.addClass(this.container, 'p-focus');
    this.focus = true;
    this.hide();

    if (this.props.onFocus) {
      this.props.onFocus(event);
    }
  }

  onOptionClick(event) {
    const option = event.option;

    if (!option.disabled) {
      this.selectItem(event);
      this.focusInput.focus();
    }

    setTimeout(() => {
      this.hide();
    }, 100);
  }

  onFilterInputChange(event) {
    this.setState({
      filter: event.target.value
    });
  }

  resetFilter() {
    this.setState({
      filter: ''
    });
  }

  onFilterInputKeyDown(event) {
    switch (event.which) {
      //down
      case 40:
        this.onDownKey(event);
        break;
      //up

      case 38:
        this.onUpKey(event);
        break;
      //enter

      case 13:
        this.hide();
        event.preventDefault();
        break;

      default:
        break;
    }
  }

  clear(event) {
    this.props.onChange({
      originalEvent: event,
      value: null,
      stopPropagation: () => {},
      preventDefault: () => {},
      target: {
        name: this.props.name,
        id: this.props.id,
        value: null
      }
    });
    this.updateEditableLabel();
  }

  selectItem(event) {
    let currentSelectedOption = this.findOption(this.props.value);

    if (currentSelectedOption !== event.option) {
      this.updateEditableLabel(event.option);
      const optionValue = this.getOptionValue(event.option);
      this.props.onChange({
        originalEvent: event.originalEvent,
        value: optionValue,
        stopPropagation: () => {},
        preventDefault: () => {},
        target: {
          name: this.props.name,
          id: this.props.id,
          value: optionValue
        }
      });
    }
  }

  findOptionIndex(value) {
    let index = -1;

    if (this.props.options) {
      for (let i = 0; i < this.props.options.length; i++) {
        let optionValue = this.getOptionValue(this.props.options[i]);

        if (value === null && optionValue == null || ObjectUtils.equals(value, optionValue, this.props.dataKey)) {
          index = i;
          break;
        }
      }
    }

    return index;
  }

  findOption(value) {
    let index = this.findOptionIndex(value);
    return index !== -1 ? this.props.options[index] : null;
  }

  show() {
    this.panel.element.style.zIndex = String(DomHandler.generateZIndex());
    this.panel.element.style.display = 'block';
    setTimeout(() => {
      DomHandler.addClass(this.panel.element, 'p-input-overlay-visible');
      DomHandler.removeClass(this.panel.element, 'p-input-overlay-hidden');
    }, 1);
    this.alignPanel();
    this.bindDocumentClickListener();
    this.setState({
      overlayVisible: true
    });
  }

  hide() {
    if (this.panel && this.panel.element && this.panel.element.offsetParent) {
      DomHandler.addClass(this.panel.element, 'p-input-overlay-hidden');
      DomHandler.removeClass(this.panel.element, 'p-input-overlay-visible');
      this.unbindDocumentClickListener();
      this.hideTimeout = setTimeout(() => {
        this.panel.element.style.display = 'none';
        DomHandler.removeClass(this.panel.element, 'p-input-overlay-hidden');
      }, 150);
      this.setState({
        overlayVisible: false
      });
    }
  }

  alignPanel() {
    if (this.props.appendTo) {
      this.panel.element.style.minWidth = DomHandler.getWidth(this.container) + 'px';
      DomHandler.absolutePosition(this.panel.element, this.container);
    } else {
      DomHandler.relativePosition(this.panel.element, this.container);
    }
  }

  bindDocumentClickListener() {
    if (!this.documentClickListener) {
      this.documentClickListener = event => {
        if (this.isOutsideClicked(event)) {
          this.hide();
        }
      };

      document.addEventListener('click', this.documentClickListener);
    }
  }

  unbindDocumentClickListener() {
    if (this.documentClickListener) {
      document.removeEventListener('click', this.documentClickListener);
      this.documentClickListener = null;
    }
  }

  isOutsideClicked(event) {
    return this.container && !(this.container.isSameNode(event.target) || this.isClearClicked(event) || this.container.contains(event.target) || this.panel && this.panel.element && this.panel.element.contains(event.target));
  }

  isClearClicked(event) {
    return DomHandler.hasClass(event.target, 'p-dropdown-clear-icon');
  }

  updateEditableLabel(option) {
    if (this.editableInput) {
      this.editableInput.value = option ? this.getOptionLabel(option) : this.props.value || '';
    }
  }

  hasFilter() {
    return this.state.filter && this.state.filter.trim().length > 0;
  }

  renderHiddenSelect(selectedOption) {
    let placeHolderOption = _("option", {
      value: ""
    }, this.props.placeholder);
    let option = selectedOption ? _("option", {
      value: selectedOption.value
    }, this.getOptionLabel(selectedOption)) : null;
    return _("div", {
      className: "p-hidden-accessible p-dropdown-hidden-select"
    }, _("select", {
      ref: el => this.nativeSelect = el,
      required: this.props.required,
      name: this.props.name,
      tabIndex: "-1",
      "aria-hidden": "true"
    }, placeHolderOption, option));
  }

  renderKeyboardHelper() {
    return _("div", {
      className: "p-hidden-accessible"
    }, _("input", {
      ref: el => this.focusInput = el,
      id: this.props.inputId,
      type: "text",
      readOnly: true,
      "aria-haspopup": "listbox",
      onFocus: this.onInputFocus,
      onBlur: this.onInputBlur,
      onKeyDown: this.onInputKeyDown,
      disabled: this.props.disabled,
      tabIndex: this.props.tabIndex,
      "aria-label": this.props.ariaLabel,
      "aria-labelledby": this.props.ariaLabelledBy
    }));
  }

  renderLabel(label) {
    if (this.props.editable) {
      let value = label || this.props.value || '';
      return _("input", {
        ref: el => this.editableInput = el,
        type: "text",
        defaultValue: value,
        className: "p-dropdown-label p-inputtext",
        disabled: this.props.disabled,
        placeholder: this.props.placeholder,
        maxLength: this.props.maxLength,
        onClick: this.onEditableInputClick,
        onInput: this.onEditableInputChange,
        onFocus: this.onEditableInputFocus,
        onBlur: this.onInputBlur,
        "aria-label": this.props.ariaLabel,
        "aria-labelledby": this.props.ariaLabelledBy,
        "aria-haspopup": "listbox"
      });
    } else {
      let className = classNames('p-dropdown-label p-inputtext', {
        'p-placeholder': label === null && this.props.placeholder,
        'p-dropdown-label-empty': label === null && !this.props.placeholder
      });
      return _("label", {
        className: className
      }, label || this.props.placeholder || 'empty');
    }
  }

  renderClearIcon() {
    if (this.props.value != null && this.props.showClear && !this.props.disabled) {
      return _("i", {
        className: "p-dropdown-clear-icon pi pi-times",
        onClick: this.clear
      });
    } else {
      return null;
    }
  }

  renderDropdownIcon() {
    return _("div", {
      className: "p-dropdown-trigger",
      role: "button",
      "aria-haspopup": "listbox",
      "aria-expanded": this.state.overlayVisible
    }, _("span", {
      className: "p-dropdown-trigger-icon pi pi-chevron-down p-clickable"
    }));
  }

  renderItems(selectedOption) {
    let items = this.props.options;

    if (items && this.hasFilter()) {
      items = this.filter(items);
    }

    if (items) {
      return items.map(option => {
        let optionLabel = this.getOptionLabel(option);
        return _(DropdownItem, {
          key: this.getOptionKey(option),
          label: optionLabel,
          option: option,
          template: this.props.itemTemplate,
          selected: selectedOption === option,
          disabled: option.disabled,
          onClick: this.onOptionClick
        });
      });
    } else {
      return null;
    }
  }

  renderFilter() {
    if (this.props.filter) {
      return _("div", {
        className: "p-dropdown-filter-container"
      }, _("input", {
        ref: el => this.filterInput = el,
        type: "text",
        autoComplete: "off",
        className: "p-dropdown-filter p-inputtext p-component",
        placeholder: this.props.filterPlaceholder,
        onKeyDown: this.onFilterInputKeyDown,
        onChange: this.onFilterInputChange,
        value: this.state.filter
      }), _("span", {
        className: "p-dropdown-filter-icon pi pi-search"
      }));
    } else {
      return null;
    }
  }

  getOptionLabel(option) {
    return this.props.optionLabel ? ObjectUtils.resolveFieldData(option, this.props.optionLabel) : option['label'] !== undefined ? option['label'] : option;
  }

  getOptionValue(option) {
    return this.props.optionValue ? ObjectUtils.resolveFieldData(option, this.props.optionValue) : option['value'] !== undefined ? option['value'] : option;
  }

  getOptionKey(option) {
    return this.props.dataKey ? ObjectUtils.resolveFieldData(option, this.props.dataKey) : this.getOptionLabel(option);
  }

  checkValidity() {
    return this.nativeSelect.checkValidity();
  }

  componentDidMount() {
    if (this.props.autoFocus && this.focusInput) {
      this.focusInput.focus();
    }

    if (this.props.tooltip) {
      this.renderTooltip();
    }

    this.nativeSelect.selectedIndex = 1;
  }

  componentWillUnmount() {
    this.unbindDocumentClickListener();

    if (this.tooltip) {
      this.tooltip.destroy();
      this.tooltip = null;
    }

    if (this.hideTimeout) {
      clearTimeout(this.hideTimeout);
      this.hideTimeout = null;
    }
  }

  componentDidUpdate(prevProps, prevState) {
    if (this.props.filter) {
      this.alignPanel();
    }

    if (this.panel.element.offsetParent) {
      let highlightItem = DomHandler.findSingle(this.panel.element, 'li.p-highlight');

      if (highlightItem) {
        DomHandler.scrollInView(this.panel.itemsWrapper, highlightItem);
      }
    }

    if (prevProps.tooltip !== this.props.tooltip) {
      if (this.tooltip) this.tooltip.updateContent(this.props.tooltip);else this.renderTooltip();
    }

    if (this.state.filter && (!this.props.options || this.props.options.length === 0)) {
      this.setState({
        filter: ''
      });
    }

    this.nativeSelect.selectedIndex = 1;
  }

  renderTooltip() {
    this.tooltip = new Tooltip({
      target: this.container,
      content: this.props.tooltip,
      options: this.props.tooltipOptions
    });
  }

  render() {
    let className = classNames('p-dropdown p-component', this.props.className, {
      'p-disabled': this.props.disabled,
      'p-inputwrapper-filled': this.props.value,
      'p-inputwrapper-focus': this.focus,
      'p-dropdown-clearable': this.props.showClear && !this.props.disabled
    });
    let selectedOption = this.findOption(this.props.value);
    let label = selectedOption ? this.getOptionLabel(selectedOption) : null;
    let hiddenSelect = this.renderHiddenSelect(selectedOption);
    let keyboardHelper = this.renderKeyboardHelper();
    let labelElement = this.renderLabel(label);
    let dropdownIcon = this.renderDropdownIcon();
    let items = this.renderItems(selectedOption);
    let filterElement = this.renderFilter();
    let clearIcon = this.renderClearIcon();

    if (this.props.editable && this.editableInput) {
      let value = label || this.props.value || '';
      this.editableInput.value = value;
    }

    return _("div", {
      id: this.props.id,
      ref: el => this.container = el,
      className: className,
      style: this.props.style,
      onClick: this.onClick,
      onMouseDown: this.props.onMouseDown,
      onContextMenu: this.props.onContextMenu
    }, keyboardHelper, hiddenSelect, labelElement, clearIcon, dropdownIcon, _(DropdownPanel, {
      ref: el => this.panel = el,
      appendTo: this.props.appendTo,
      panelStyle: this.props.panelStyle,
      panelClassName: this.props.panelClassName,
      scrollHeight: this.props.scrollHeight,
      filter: filterElement,
      onClick: this.onPanelClick
    }, items));
  }

}
Dropdown.defaultProps = {
	id: null,
	name: null,
	value: null,
	options: null,
	optionLabel: null,
	optionValue: null,
	itemTemplate: null,
	style: null,
	className: null,
	scrollHeight: '200px',
	filter: false,
	filterBy: null,
	filterMatchMode: 'contains',
	filterPlaceholder: null,
	filterLocale: undefined,
	editable: false,
	placeholder:null,
	required: false,
	disabled: false,
	appendTo: null,
	tabIndex: null,
	autoFocus: false,
	filterInputAutoFocus: true,
	panelClassName: null,
	panelStyle: null,
	dataKey: null,
	inputId: null,
	showClear: false,
	maxLength: null,
	tooltip: null,
	tooltipOptions: null,
	ariaLabel: null,
	ariaLabelledBy: null,
	onChange: null,
	onFocus: null,
	onBlur: null,
	onMouseDown: null,
	onContextMenu: null
};

Dropdown.propTypes = {
	id: PropTypes.string,
	name: PropTypes.string,
	value: PropTypes.any,
	options: PropTypes.array,
	optionLabel: PropTypes.string,
	optionValue: PropTypes.string,
	itemTemplate: PropTypes.func,
	style: PropTypes.object,
	className: PropTypes.string,
	scrollHeight: PropTypes.string,
	filter: PropTypes.bool,
	filterBy: PropTypes.string,
	filterMatchMode: PropTypes.string,
	filterPlaceholder: PropTypes.string,
	filterLocale: PropTypes.string,
	editable:PropTypes.bool,
	placeholder: PropTypes.string,
	required: PropTypes.bool,
	disabled: PropTypes.bool,
	appendTo: PropTypes.any,
	tabIndex: PropTypes.number,
	autoFocus: PropTypes.bool,
	filterInputAutoFocus: PropTypes.bool,
	lazy: PropTypes.bool,
	panelClassName: PropTypes.string,
	panelStyle: PropTypes.object,
	dataKey: PropTypes.string,
	inputId: PropTypes.string,
	showClear: PropTypes.bool,
	maxLength: PropTypes.number,
	tooltip: PropTypes.string,
	tooltipOptions: PropTypes.object,
	ariaLabel: PropTypes.string,
	ariaLabelledBy: PropTypes.string,
	onChange: PropTypes.func,
	onFocus: PropTypes.func,
	onBlur: PropTypes.func,
	onMouseDown: PropTypes.func,
	onContextMenu: PropTypes.func
};

class DropdownItem extends React.Component {
  constructor(props) {
    super(props);
    this.onClick = this.onClick.bind(this);
  }

  onClick(event) {
    if (this.props.onClick) {
      this.props.onClick({
        originalEvent: event,
        option: this.props.option
      });
    }
  }

  render() {
    let className = classNames(this.props.option.className, 'p-dropdown-item', {
      'p-highlight': this.props.selected,
      'p-disabled': this.props.disabled,
      'p-dropdown-item-empty': !this.props.label || this.props.label.length === 0
    });
    let content = this.props.template ? this.props.template(this.props.option) : this.props.label;
    return _("li", {
      className: className,
      onClick: this.onClick,
      "aria-label": this.props.label,
      key: this.props.label,
      role: "option",
      "aria-selected": this.props.selected
    }, content);
  }

}

DropdownItem.defaultProps = {
	option: null,
	label: null,
	template: null,
	selected: false,
	disabled: false,
	onClick: null
};

DropdownItem.propTypes = {
	option: PropTypes.any,
	label: PropTypes.any,
	template: PropTypes.func,
	selected: PropTypes.bool,
	disabled: PropTypes.bool,
	onClick: PropTypes.func
};

class DropdownPanel extends React.Component {
  renderElement() {
    let className = classNames('p-dropdown-panel p-hidden p-input-overlay', this.props.panelClassName);
    return _("div", {
      ref: el => this.element = el,
      className: className,
      style: this.props.panelStyle,
      onClick: this.props.onClick
    }, this.props.filter, _("div", {
      ref: el => this.itemsWrapper = el,
      className: "p-dropdown-items-wrapper",
      style: {
        maxHeight: this.props.scrollHeight || 'auto'
      }
    }, _("ul", {
      className: "p-dropdown-items p-dropdown-list p-component",
      role: "listbox"
    }, this.props.children)));
  }

  render() {
    let element = this.renderElement();

    if (this.props.appendTo) {
      return ReactDOM.createPortal(element, this.props.appendTo);
    } else {
      return element;
    }
  }

}

DropdownPanel.defaultProps = {
	appendTo: null,
	filter: null,
	scrollHeight: null,
	panelClassName: null,
	panelStyle: null,
	onClick: null
};

DropdownPanel.propTypes = {
	appendTo: PropTypes.object,
	filter: PropTypes.any,
	scrollHeight: PropTypes.string,
	panelClassName: PropTypes.string,
	panelStyle: PropTypes.object,
	onClick: PropTypes.func
};

class InputMask extends React.Component {
  constructor(props) {
    super(props);
    this.onFocus = this.onFocus.bind(this);
    this.onBlur = this.onBlur.bind(this);
    this.onKeyDown = this.onKeyDown.bind(this);
    this.onKeyPress = this.onKeyPress.bind(this);
    this.onInput = this.onInput.bind(this);
    this.handleInputChange = this.handleInputChange.bind(this);
  }

  caret(first, last) {
    let range, begin, end;

    if (!this.input.offsetParent || this.input !== document.activeElement) {
      return;
    }

    if (typeof first === 'number') {
      begin = first;
      end = typeof last === 'number' ? last : begin;

      if (this.input.setSelectionRange) {
        this.input.setSelectionRange(begin, end);
      } else if (this.input['createTextRange']) {
        range = this.input['createTextRange']();
        range.collapse(true);
        range.moveEnd('character', end);
        range.moveStart('character', begin);
        range.select();
      }
    } else {
      if (this.input.setSelectionRange) {
        begin = this.input.selectionStart;
        end = this.input.selectionEnd;
      } else if (document['selection'] && document['selection'].createRange) {
        range = document['selection'].createRange();
        begin = 0 - range.duplicate().moveStart('character', -100000);
        end = begin + range.text.length;
      }

      return {
        begin: begin,
        end: end
      };
    }
  }

  isCompleted() {
    for (let i = this.firstNonMaskPos; i <= this.lastRequiredNonMaskPos; i++) {
      if (this.tests[i] && this.buffer[i] === this.getPlaceholder(i)) {
        return false;
      }
    }

    return true;
  }

  getPlaceholder(i) {
    if (i < this.props.slotChar.length) {
      return this.props.slotChar.charAt(i);
    }

    return this.props.slotChar.charAt(0);
  }

  getValue() {
    return this.props.unmask ? this.getUnmaskedValue() : this.input && this.input.value;
  }

  seekNext(pos) {
    while (++pos < this.len && !this.tests[pos]);

    return pos;
  }

  seekPrev(pos) {
    while (--pos >= 0 && !this.tests[pos]);

    return pos;
  }

  shiftL(begin, end) {
    let i, j;

    if (begin < 0) {
      return;
    }

    for (i = begin, j = this.seekNext(end); i < this.len; i++) {
      if (this.tests[i]) {
        if (j < this.len && this.tests[i].test(this.buffer[j])) {
          this.buffer[i] = this.buffer[j];
          this.buffer[j] = this.getPlaceholder(j);
        } else {
          break;
        }

        j = this.seekNext(j);
      }
    }

    this.writeBuffer();
    this.caret(Math.max(this.firstNonMaskPos, begin));
  }

  shiftR(pos) {
    let i, c, j, t;

    for (i = pos, c = this.getPlaceholder(pos); i < this.len; i++) {
      if (this.tests[i]) {
        j = this.seekNext(i);
        t = this.buffer[i];
        this.buffer[i] = c;

        if (j < this.len && this.tests[j].test(t)) {
          c = t;
        } else {
          break;
        }
      }
    }
  }

  handleAndroidInput(e) {
    var curVal = this.input.value;
    var pos = this.caret();

    if (this.oldVal && this.oldVal.length && this.oldVal.length > curVal.length) {
      // a deletion or backspace happened
      this.checkVal(true);

      while (pos.begin > 0 && !this.tests[pos.begin - 1]) pos.begin--;

      if (pos.begin === 0) {
        while (pos.begin < this.firstNonMaskPos && !this.tests[pos.begin]) pos.begin++;
      }

      this.caret(pos.begin, pos.begin);
    } else {
      this.checkVal(true);

      while (pos.begin < this.len && !this.tests[pos.begin]) pos.begin++;

      this.caret(pos.begin, pos.begin);
    }

    if (this.props.onComplete && this.isCompleted()) {
      this.props.onComplete({
        originalEvent: e,
        value: this.getValue()
      });
    }
  }

  onBlur(e) {
    this.focus = false;
    this.checkVal();
    this.updateModel(e);
    this.updateFilledState();

    if (this.input.value !== this.focusText) {
      let event = document.createEvent('HTMLEvents');
      event.initEvent('change', true, false);
      this.input.dispatchEvent(event);
    }
  }

  onKeyDown(e) {
    if (this.props.readonly) {
      return;
    }

    let k = e.which || e.keyCode,
        pos,
        begin,
        end;
    let iPhone = /iphone/i.test(DomHandler.getUserAgent());
    this.oldVal = this.input.value; //backspace, delete, and escape get special treatment

    if (k === 8 || k === 46 || iPhone && k === 127) {
      pos = this.caret();
      begin = pos.begin;
      end = pos.end;

      if (end - begin === 0) {
        begin = k !== 46 ? this.seekPrev(begin) : end = this.seekNext(begin - 1);
        end = k === 46 ? this.seekNext(end) : end;
      }

      this.clearBuffer(begin, end);
      this.shiftL(begin, end - 1);
      this.updateModel(e);
      e.preventDefault();
    } else if (k === 13) {
      // enter
      this.onBlur(e);
      this.updateModel(e);
    } else if (k === 27) {
      // escape
      this.input.value = this.focusText;
      this.caret(0, this.checkVal());
      this.updateModel(e);
      e.preventDefault();
    }
  }

  onKeyPress(e) {
    if (this.props.readonly) {
      return;
    }

    var k = e.which || e.keyCode,
        pos = this.caret(),
        p,
        c,
        next,
        completed;

    if (e.ctrlKey || e.altKey || e.metaKey || k < 32) {
      //Ignore
      return;
    } else if (k && k !== 13) {
      if (pos.end - pos.begin !== 0) {
        this.clearBuffer(pos.begin, pos.end);
        this.shiftL(pos.begin, pos.end - 1);
      }

      p = this.seekNext(pos.begin - 1);

      if (p < this.len) {
        c = String.fromCharCode(k);

        if (this.tests[p].test(c)) {
          this.shiftR(p);
          this.buffer[p] = c;
          this.writeBuffer();
          next = this.seekNext(p);

          if (/android/i.test(DomHandler.getUserAgent())) {
            //Path for CSP Violation on FireFox OS 1.1
            let proxy = () => {
              this.caret(next);
            };

            setTimeout(proxy, 0);
          } else {
            this.caret(next);
          }

          if (pos.begin <= this.lastRequiredNonMaskPos) {
            completed = this.isCompleted();
          }
        }
      }

      e.preventDefault();
    }

    this.updateModel(e);

    if (this.props.onComplete && completed) {
      this.props.onComplete({
        originalEvent: e,
        value: this.getValue()
      });
    }
  }

  clearBuffer(start, end) {
    let i;

    for (i = start; i < end && i < this.len; i++) {
      if (this.tests[i]) {
        this.buffer[i] = this.getPlaceholder(i);
      }
    }
  }

  writeBuffer() {
    this.input.value = this.buffer.join('');
  }

  checkVal(allow) {
    this.isValueChecked = true; //try to place characters where they belong

    let test = this.input.value,
        lastMatch = -1,
        i,
        c,
        pos;

    for (i = 0, pos = 0; i < this.len; i++) {
      if (this.tests[i]) {
        this.buffer[i] = this.getPlaceholder(i);

        while (pos++ < test.length) {
          c = test.charAt(pos - 1);

          if (this.tests[i].test(c)) {
            this.buffer[i] = c;
            lastMatch = i;
            break;
          }
        }

        if (pos > test.length) {
          this.clearBuffer(i + 1, this.len);
          break;
        }
      } else {
        if (this.buffer[i] === test.charAt(pos)) {
          pos++;
        }

        if (i < this.partialPosition) {
          lastMatch = i;
        }
      }
    }

    if (allow) {
      this.writeBuffer();
    } else if (lastMatch + 1 < this.partialPosition) {
      if (this.props.autoClear || this.buffer.join('') === this.defaultBuffer) {
        // Invalid value. Remove it and replace it with the
        // mask, which is the default behavior.
        if (this.input.value) this.input.value = '';
        this.clearBuffer(0, this.len);
      } else {
        // Invalid value, but we opt to show the value to the
        // user and allow them to correct their mistake.
        this.writeBuffer();
      }
    } else {
      this.writeBuffer();
      this.input.value = this.input.value.substring(0, lastMatch + 1);
    }

    return this.partialPosition ? i : this.firstNonMaskPos;
  }

  onFocus(event) {
    if (this.props.readonly) {
      return;
    }

    this.focus = true;
    clearTimeout(this.caretTimeoutId);
    let pos;
    this.focusText = this.input.value;
    pos = this.checkVal();
    this.caretTimeoutId = setTimeout(() => {
      if (this.input !== document.activeElement) {
        return;
      }

      this.writeBuffer();

      if (pos === this.props.mask.replace("?", "").length) {
        this.caret(0, pos);
      } else {
        this.caret(pos);
      }

      this.updateFilledState();
    }, 10);
  }

  onInput(event) {
    if (this.androidChrome) this.handleAndroidInput(event);else this.handleInputChange(event);
  }

  handleInputChange(e) {
    if (this.props.readonly) {
      return;
    }

    var pos = this.checkVal(true);
    this.caret(pos);
    this.updateModel(e);

    if (this.props.onComplete && this.isCompleted()) {
      this.props.onComplete({
        originalEvent: e,
        value: this.getValue()
      });
    }
  }

  getUnmaskedValue() {
    let unmaskedBuffer = [];

    for (let i = 0; i < this.buffer.length; i++) {
      let c = this.buffer[i];

      if (this.tests[i] && c !== this.getPlaceholder(i)) {
        unmaskedBuffer.push(c);
      }
    }

    return unmaskedBuffer.join('');
  }

  updateModel(e) {
    if (this.props.onChange) {
      var val = this.props.unmask ? this.getUnmaskedValue() : e && e.target.value;
      this.props.onChange({
        originalEvent: e,
        value: this.defaultBuffer !== val ? val : '',
        stopPropagation: () => {},
        preventDefault: () => {},
        target: {
          name: this.props.name,
          id: this.props.id,
          value: this.defaultBuffer !== val ? val : ''
        }
      });
    }
  }

  updateFilledState() {
    if (this.input && this.input.value && this.input.value.length > 0) DomHandler.addClass(this.input, 'p-filled');else DomHandler.removeClass(this.input, 'p-filled');
  }

  updateValue() {
    if (this.input) {
      if (this.props.value == null) {
        this.input.value = '';
      } else {
        this.input.value = this.props.value;
        this.checkVal();
        setTimeout(() => {
          if (this.input) {
            this.writeBuffer();
            this.checkVal();
          }
        }, 10);
      }

      this.focusText = this.input.value;
    }

    this.updateFilledState();
  }

  isValueUpdated() {
    return this.props.unmask ? this.props.value !== this.getUnmaskedValue() : this.defaultBuffer !== this.input.value && this.input.value !== this.props.value;
  }

  init() {
    this.tests = [];
    this.partialPosition = this.props.mask.length;
    this.len = this.props.mask.length;
    this.firstNonMaskPos = null;
    this.defs = {
      '9': '[0-9]',
      'a': '[A-Za-z]',
      '*': '[A-Za-z0-9]'
    };
    let ua = DomHandler.getUserAgent();
    this.androidChrome = /chrome/i.test(ua) && /android/i.test(ua);
    let maskTokens = this.props.mask.split('');

    for (let i = 0; i < maskTokens.length; i++) {
      let c = maskTokens[i];

      if (c === '?') {
        this.len--;
        this.partialPosition = i;
      } else if (this.defs[c]) {
        this.tests.push(new RegExp(this.defs[c]));

        if (this.firstNonMaskPos === null) {
          this.firstNonMaskPos = this.tests.length - 1;
        }

        if (i < this.partialPosition) {
          this.lastRequiredNonMaskPos = this.tests.length - 1;
        }
      } else {
        this.tests.push(null);
      }
    }

    this.buffer = [];

    for (let i = 0; i < maskTokens.length; i++) {
      let c = maskTokens[i];

      if (c !== '?') {
        if (this.defs[c]) this.buffer.push(this.getPlaceholder(i));else this.buffer.push(c);
      }
    }

    this.defaultBuffer = this.buffer.join('');
  }

  componentDidMount() {
    this.init();
    this.updateValue();

    if (this.props.tooltip) {
      this.renderTooltip();
    }
  }

  componentDidUpdate(prevProps) {
    if (prevProps.tooltip !== this.props.tooltip) {
      if (this.tooltip) this.tooltip.updateContent(this.props.tooltip);else this.renderTooltip();
    }

    if (this.isValueUpdated()) {
      this.updateValue();
    }

    if (prevProps.mask !== this.props.mask) {
      this.init();
      this.updateValue();
      this.updateModel();
    }
  }

  componentWillUnmount() {
    if (this.tooltip) {
      this.tooltip.destroy();
      this.tooltip = null;
    }
  }

  renderTooltip() {
    this.tooltip = new Tooltip({
      target: this.input,
      content: this.props.tooltip,
      options: this.props.tooltipOptions
    });
  }

  render() {
    return _(InputText, {
      id: this.props.id,
      ref: el => this.input = ReactDOM.findDOMNode(el),
      type: this.props.type,
      name: this.props.name,
      style: this.props.style,
      className: this.props.className,
      placeholder: this.props.placeholder,
      size: this.props.size,
      maxLength: this.props.maxlength,
      tabIndex: this.props.tabindex,
      disabled: this.props.disabled,
      readOnly: this.props.readonly,
      onFocus: this.onFocus,
      onBlur: this.onBlur,
      onKeyDown: this.onKeyDown,
      onKeyPress: this.onKeyPress,
      onInput: this.onInput,
      onPaste: this.handleInputChange,
      required: this.props.required,
      "aria-labelledby": this.props.ariaLabelledBy
    });
  }

}

InputMask.defaultProps = {
	id: null,
	value: null,
	type: 'text',
	mask: null,
	slotChar: '_',
	autoClear: true,
	unmask: false,
	style: null,
	className: null,
	placeholder: null,
	size: null,
	maxlength: null,
	tabindex: null,
	disabled: false,
	readonly: false,
	name: null,
	required: false,
	tooltip: null,
	tooltipOptions: null,
	ariaLabelledBy: null,
	onComplete: null,
	onChange: null
}

InputMask.propTypes = {
	id: PropTypes.string,
	value: PropTypes.string,
	type: PropTypes.string,
	mask: PropTypes.string,
	slotChar: PropTypes.string,
	autoClear: PropTypes.bool,
	unmask: PropTypes.bool,
	style: PropTypes.object,
	className: PropTypes.string,
	placeholder: PropTypes.string,
	size: PropTypes.number,
	maxlength: PropTypes.number,
	tabindex: PropTypes.number,
	disabled: PropTypes.bool,
	readonly: PropTypes.bool,
	name: PropTypes.string,
	required: PropTypes.bool,
	tooltip: PropTypes.string,
	tooltipOptions: PropTypes.object,
	ariaLabelledBy: PropTypes.string,
	onComplete: PropTypes.func,
	onChange: PropTypes.func
}
class InputNumber extends React.Component {
  constructor(props) {
    super(props);
    this.constructParser();
    this.onInput = this.onInput.bind(this);
    this.onInputKeyDown = this.onInputKeyDown.bind(this);
    this.onInputKeyPress = this.onInputKeyPress.bind(this);
    this.onInputClick = this.onInputClick.bind(this);
    this.onInputBlur = this.onInputBlur.bind(this);
    this.onInputFocus = this.onInputFocus.bind(this);
    this.onPaste = this.onPaste.bind(this);
    this.onUpButtonMouseLeave = this.onUpButtonMouseLeave.bind(this);
    this.onUpButtonMouseDown = this.onUpButtonMouseDown.bind(this);
    this.onUpButtonMouseUp = this.onUpButtonMouseUp.bind(this);
    this.onUpButtonKeyDown = this.onUpButtonKeyDown.bind(this);
    this.onUpButtonKeyUp = this.onUpButtonKeyUp.bind(this);
    this.onDownButtonMouseLeave = this.onDownButtonMouseLeave.bind(this);
    this.onDownButtonMouseDown = this.onDownButtonMouseDown.bind(this);
    this.onDownButtonMouseUp = this.onDownButtonMouseUp.bind(this);
    this.onDownButtonKeyDown = this.onDownButtonKeyDown.bind(this);
    this.onDownButtonKeyUp = this.onDownButtonKeyUp.bind(this);
  }

  getOptions() {
    return {
      localeMatcher: this.props.localeMatcher,
      style: this.props.mode,
      currency: this.props.currency,
      currencyDisplay: this.props.currencyDisplay,
      useGrouping: this.props.useGrouping,
      minimumFractionDigits: this.props.minFractionDigits,
      maximumFractionDigits: this.props.maxFractionDigits
    };
  }

  constructParser() {
    this.numberFormat = new Intl.NumberFormat(this.props.locale, this.getOptions());
    const numerals = [...new Intl.NumberFormat(this.props.locale, {
      useGrouping: false
    }).format(9876543210)].reverse();
    const index = new Map(numerals.map((d, i) => [d, i]));
    this._numeral = new RegExp(`[${numerals.join('')}]`, 'g');
    this._decimal = this.getDecimalExpression();
    this._group = this.getGroupingExpression();
    this._minusSign = this.getMinusSignExpression();
    this._currency = this.getCurrencyExpression();
    this._suffix = new RegExp(`[${this.props.suffix || ''}]`, 'g');
    this._prefix = new RegExp(`[${this.props.prefix || ''}]`, 'g');

    this._index = d => index.get(d);
  }

  getDecimalExpression() {
    const formatter = new Intl.NumberFormat(this.props.locale, {
      useGrouping: false
    });
    return new RegExp(`[${formatter.format(1.1).trim().replace(this._numeral, '')}]`, 'g');
  }

  getGroupingExpression() {
    const formatter = new Intl.NumberFormat(this.props.locale, {
      useGrouping: true
    });
    return new RegExp(`[${formatter.format(1000).trim().replace(this._numeral, '')}]`, 'g');
  }

  getMinusSignExpression() {
    const formatter = new Intl.NumberFormat(this.props.locale, {
      useGrouping: false
    });
    return new RegExp(`[${formatter.format(-1).trim().replace(this._numeral, '')}]`, 'g');
  }

  getCurrencyExpression() {
    if (this.props.currency) {
      const formatter = new Intl.NumberFormat(this.props.locale, {
        style: 'currency',
        currency: this.props.currency,
        currencyDisplay: this.props.currencyDisplay
      });
      return new RegExp(`[${formatter.format(1).replace(/\s/g, '').replace(this._numeral, '').replace(this._decimal, '').replace(this._group, '')}]`, 'g');
    } else {
      return new RegExp(`[]`, 'g');
    }
  }

  formatValue(value) {
    if (value != null) {
      if (this.props.format) {
        let formatter = new Intl.NumberFormat(this.props.locale, this.getOptions());
        let formattedValue = formatter.format(value);

        if (this.props.prefix) {
          formattedValue = this.props.prefix + formattedValue;
        }

        if (this.props.suffix) {
          formattedValue = formattedValue + this.props.suffix;
        }

        return formattedValue;
      }

      return value;
    }

    return '';
  }

  parseValue(text) {
    let filteredText = text.trim().replace(/\s/g, '').replace(this._currency, '').replace(this._group, '').replace(this._suffix, '').replace(this._prefix, '').replace(this._minusSign, '-').replace(this._decimal, '.').replace(this._numeral, this._index);

    if (filteredText) {
      let parsedValue = +filteredText;
      return isNaN(parsedValue) ? null : parsedValue;
    } else {
      return null;
    }
  }

  repeat(event, interval, dir) {
    let i = interval || 500;
    this.clearTimer();
    this.timer = setTimeout(() => {
      this.repeat(event, 40, dir);
    }, i);
    this.spin(event, dir);
  }

  spin(event, dir) {
    let step = this.props.step * dir;
    let currentValue = this.props.value || 0;
    let newValue = currentValue + step;

    if (this.props.min !== null && newValue < this.props.min) {
      newValue = this.props.min;
    }

    if (this.props.max !== null && newValue > this.props.max) {
      newValue = this.props.max;
    }

    this.updateInput(newValue, 'spin');
    this.updateModel(event, newValue);
  }

  onUpButtonMouseDown(event) {
    if (!this.props.disabled) {
      this.inputEl.focus();
      this.repeat(event, null, 1);
      event.preventDefault();
    }
  }

  onUpButtonMouseUp() {
    if (!this.props.disabled) {
      this.clearTimer();
    }
  }

  onUpButtonMouseLeave() {
    if (!this.props.disabled) {
      this.clearTimer();
    }
  }

  onUpButtonKeyUp() {
    if (!this.props.disabled) {
      this.clearTimer();
    }
  }

  onUpButtonKeyDown(event) {
    if (event.keyCode === 32 || event.keyCode === 13) {
      this.repeat(event, null, 1);
    }
  }

  onDownButtonMouseDown(event, focusInput) {
    if (!this.props.disabled) {
      this.inputEl.focus();
      this.repeat(event, null, -1);
      event.preventDefault();
    }
  }

  onDownButtonMouseUp() {
    if (!this.props.disabled) {
      this.clearTimer();
    }
  }

  onDownButtonMouseLeave() {
    if (!this.props.disabled) {
      this.clearTimer();
    }
  }

  onDownButtonKeyUp() {
    if (!this.props.disabled) {
      this.clearTimer();
    }
  }

  onDownButtonKeyDown(event) {
    if (event.keyCode === 32 || event.keyCode === 13) {
      this.repeat(event, null, -1);
    }
  }

  onInput(event) {
    if (this.isSpecialChar) {
      event.target.value = this.lastValue;
    }

    this.isSpecialChar = false;
  }

  onInputKeyDown(event) {
    this.lastValue = event.target.value;

    if (event.shiftKey || event.altKey) {
      this.isSpecialChar = true;
      return;
    }

    let selectionStart = event.target.selectionStart;
    let selectionEnd = event.target.selectionEnd;
    let inputValue = event.target.value;

    if (event.altKey) {
      event.preventDefault();
    }

    switch (event.which) {
      //up
      case 38:
        this.spin(event, 1);
        event.preventDefault();
        break;
      //down

      case 40:
        this.spin(event, -1);
        event.preventDefault();
        break;
      //left

      case 37:
        let prevChar = inputValue.charAt(selectionStart - 1);

        if (!this.isNumeralChar(prevChar)) {
          event.preventDefault();
        }

        break;
      //right

      case 39:
        let currentChar = inputValue.charAt(selectionStart);

        if (!this.isNumeralChar(currentChar)) {
          event.preventDefault();
        }

        break;
      //backspace

      case 8:
        event.preventDefault();
        let newValueStr = null;

        if (selectionStart === selectionEnd) {
          let deleteChar = inputValue.charAt(selectionStart - 1);
          let decimalCharIndex = inputValue.search(this._decimal);
          this._decimal.lastIndex = 0;

          if (this.isNumeralChar(deleteChar)) {
            if (this._group.test(deleteChar)) {
              this._group.lastIndex = 0;
              newValueStr = inputValue.slice(0, selectionStart - 2) + inputValue.slice(selectionStart - 1);
            } else if (this._decimal.test(deleteChar)) {
              this._decimal.lastIndex = 0;
              this.inputEl.setSelectionRange(selectionStart - 1, selectionStart - 1);
            } else if (decimalCharIndex > 0 && selectionStart > decimalCharIndex) {
              newValueStr = inputValue.slice(0, selectionStart - 1) + '0' + inputValue.slice(selectionStart);
            } else {
              newValueStr = inputValue.slice(0, selectionStart - 1) + inputValue.slice(selectionStart);
            }
          }

          if (newValueStr != null) {
            this.updateValue(event, newValueStr, 'delete-single');
          }
        } else {
          newValueStr = this.deleteRange(inputValue, selectionStart, selectionEnd);
          this.updateValue(event, newValueStr, 'delete-range');
        }

        break;

      default:
        break;
    }
  }

  onInputKeyPress(event) {
    event.preventDefault();
    let code = event.which || event.keyCode;
    let char = String.fromCharCode(code);

    if (48 <= code && code <= 57 || this.isMinusSign(char)) {
      this.insert(event, char);
    }
  }

  onPaste(event) {
    event.preventDefault();
    let data = (event.clipboardData || window['clipboardData']).getData('Text');

    if (data) {
      let filteredData = this.parseValue(data);

      if (filteredData != null) {
        this.insert(event, filteredData.toString());
      }
    }
  }

  isMinusSign(char) {
    if (this._minusSign.test(char)) {
      this._minusSign.lastIndex = 0;
      return true;
    }

    return false;
  }

  insert(event, text) {
    let selectionStart = this.inputEl.selectionStart;
    let selectionEnd = this.inputEl.selectionEnd;
    let inputValue = this.inputEl.value.trim();
    let maxFractionDigits = this.numberFormat.resolvedOptions().maximumFractionDigits;
    let newValueStr;
    let decimalCharIndex = inputValue.search(this._decimal);
    this._decimal.lastIndex = 0;

    if (decimalCharIndex > 0 && selectionStart > decimalCharIndex) {
      if (selectionStart + text.length - (decimalCharIndex + 1) <= maxFractionDigits) {
        newValueStr = inputValue.slice(0, selectionStart) + text + inputValue.slice(selectionStart + text.length);
        this.updateValue(event, newValueStr, 'insert');
      }
    } else {
      newValueStr = this.insertText(inputValue, text, selectionStart, selectionEnd);
      this.updateValue(event, newValueStr, 'insert');
    }
  }

  insertText(value, text, start, end) {
    let newValueStr;
    if (end - start === value.length) newValueStr = text;else if (start === 0) newValueStr = text + value.slice(end);else if (end === value.length) newValueStr = value.slice(0, start) + text;else newValueStr = value.slice(0, start) + text + value.slice(end);
    return newValueStr;
  }

  deleteRange(value, start, end) {
    let newValueStr;
    if (end - start === value.length) newValueStr = '';else if (start === 0) newValueStr = value.slice(end);else if (end === value.length) newValueStr = value.slice(0, start);else newValueStr = value.slice(0, start) + value.slice(end);
    return newValueStr;
  }

  initCursor() {
    let selectionStart = this.inputEl.selectionStart;
    let inputValue = this.inputEl.value;
    let valueLength = inputValue.length;
    let index = null;
    let char = inputValue.charAt(selectionStart);

    if (this.isNumeralChar(char)) {
      return;
    } //left


    let i = selectionStart - 1;

    while (i >= 0) {
      char = inputValue.charAt(i);

      if (this.isNumeralChar(char)) {
        index = i;
        break;
      } else {
        i--;
      }
    }

    if (index !== null) {
      this.inputEl.setSelectionRange(index + 1, index + 1);
    } else {
      i = selectionStart + 1;

      while (i < valueLength) {
        char = inputValue.charAt(i);

        if (this.isNumeralChar(char)) {
          index = i;
          break;
        } else {
          i++;
        }
      }

      if (index !== null) {
        this.inputEl.setSelectionRange(index, index);
      }
    }
  }

  onInputClick() {
    this.initCursor();
  }

  isNumeralChar(char) {
    if (char.length === 1 && (this._numeral.test(char) || this._decimal.test(char) || this._group.test(char) || this._minusSign.test(char))) {
      this.resetRegex();
      return true;
    } else {
      return false;
    }
  }

  resetRegex() {
    this._numeral.lastIndex = 0;
    this._decimal.lastIndex = 0;
    this._group.lastIndex = 0;
    this._minusSign.lastIndex = 0;
  }

  updateValue(event, valueStr, operation) {
    if (valueStr != null) {
      let newValue = this.parseValue(valueStr);
      let valid = this.isWithinRange(newValue);

      if (valid) {
        this.updateInput(newValue, operation);
        this.updateModel(event, newValue);
      }
    }
  }

  isWithinRange(value) {
    return value == null || (this.props.min == null || value > this.props.min) && (this.props.max == null || value < this.props.max);
  }

  updateInput(value, operation) {
    let currentLength = this.inputEl.value.length;

    if (currentLength === 0) {
      this.inputEl.value = this.formatValue(value);
      this.inputEl.setSelectionRange(0, 0);
      this.initCursor();
      this.inputEl.setSelectionRange(this.inputEl.selectionStart + 1, this.inputEl.selectionStart + 1);
    } else {
      let selectionStart = this.inputEl.selectionEnd;
      let selectionEnd = this.inputEl.selectionEnd;
      this.inputEl.value = this.formatValue(value);
      let newLength = this.inputEl.value.length;

      if (newLength === currentLength) {
        if (operation === 'insert') this.inputEl.setSelectionRange(selectionEnd + 1, selectionEnd + 1);else if (operation === 'delete-single') this.inputEl.setSelectionRange(selectionEnd - 1, selectionEnd - 1);else if (operation === 'delete-range') this.inputEl.setSelectionRange(selectionStart, selectionStart);else if (operation === 'spin') this.inputEl.setSelectionRange(selectionStart, selectionEnd);
      } else {
        selectionEnd = selectionEnd + (newLength - currentLength);
        this.inputEl.setSelectionRange(selectionEnd, selectionEnd);
      }
    }

    this.inputEl.setAttribute('aria-valuenow', value);
  }

  updateModel(event, value) {
    if (this.props.onChange) {
      this.props.onChange({
        originalEvent: event,
        value: value,
        stopPropagation: () => {},
        preventDefault: () => {},
        target: {
          name: this.props.name,
          id: this.props.id,
          value: value
        }
      });
    }
  }

  onInputFocus(event) {
    this.focus = true;

    if (this.props.onFocus) {
      this.props.onFocus(event);
    }
  }

  onInputBlur(event) {
    this.focus = false;

    if (this.props.onBlur) {
      this.props.onBlur(event);
    }
  }

  clearTimer() {
    if (this.timer) {
      clearInterval(this.timer);
    }
  }

  isStacked() {
    return this.props.showButtons && this.props.buttonLayout === 'stacked';
  }

  isHorizontal() {
    return this.props.showButtons && this.props.buttonLayout === 'horizontal';
  }

  isVertical() {
    return this.props.showButtons && this.props.buttonLayout === 'vertical';
  }

  getInputMode() {
    return this.props.inputMode || (this.props.mode === 'decimal' && !this.props.minFractionDigits ? 'numeric' : 'decimal');
  }

  componentDidMount() {
    if (this.props.tooltip) {
      this.renderTooltip();
    }
  }

  componentDidUpdate(prevProps) {
    if (prevProps.tooltip !== this.props.tooltip) {
      if (this.tooltip) this.tooltip.updateContent(this.props.tooltip);else this.renderTooltip();
    }

    const formattedValue = this.formatValue(this.props.value);

    if (this.inputEl.value !== formattedValue) {
      this.inputEl.value = formattedValue;
      this.inputEl.setAttribute('aria-valuenow', this.props.value);
    }
  }

  componentWillUnmount() {
    if (this.tooltip) {
      this.tooltip.destroy();
      this.tooltip = null;
    }
  }

  renderTooltip() {
    this.tooltip = new Tooltip({
      target: this.element,
      content: this.props.tooltip,
      options: this.props.tooltipOptions
    });
  }

  renderInputElement() {
    const className = classNames('p-inputnumber-input', this.props.inputClassName);
    const valueToRender = this.formatValue(this.props.value);
    return _(InputText, {
      ref: el => this.inputEl = ReactDOM.findDOMNode(el),
      id: this.props.inputId,
      style: this.props.inputStyle,
      role: "spinbutton",
      className: className,
      defaultValue: valueToRender,
      type: this.props.type,
      size: this.props.size,
      tabIndex: this.props.tabIndex,
      inputMode: this.getInputMode(),
      maxLength: this.props.maxlength,
      disabled: this.props.disabled,
      required: this.props.required,
      pattern: this.props.pattern,
      placeholder: this.props.placeholder,
      readOnly: this.props.readonly,
      name: this.props.name,
      onKeyDown: this.onInputKeyDown,
      onKeyPress: this.onInputKeyPress,
      onInput: this.onInput,
      onClick: this.onInputClick,
      onBlur: this.onInputBlur,
      onFocus: this.onInputFocus,
      onPaste: this.onPaste,
      "aria-valuemin": this.props.min,
      "aria-valuemax": this.props.max,
      "aria-valuenow": this.props.value,
      "aria-labelledby": this.props.ariaLabelledBy
    });
  }

  renderUpButton() {
    const className = classNames("p-inputnumber-button p-inputnumber-button-up p-button p-button-icon-only p-component", this.props.incrementButtonClassName, {
      'p-disabled': this.props.disabled
    });
    const icon = classNames('p-inputnumber-button-icon', this.props.incrementButtonIcon);
    return _("button", {
      type: "button",
      className: className,
      onMouseLeave: this.onUpButtonMouseLeave,
      onMouseDown: this.onUpButtonMouseDown,
      onMouseUp: this.onUpButtonMouseUp,
      onKeyDown: this.onUpButtonKeyDown,
      onKeyUp: this.onUpButtonKeyUp,
      disabled: this.props.disabled,
      tabIndex: "-1"
    }, _("span", {
      className: icon
    }));
  }

  renderDownButton() {
    const className = classNames("p-inputnumber-button p-inputnumber-button-down p-button p-button-icon-only p-component", this.props.decrementButtonClassName, {
      'p-disabled': this.props.disabled
    });
    const icon = classNames('p-inputnumber-button-icon', this.props.decrementButtonIcon);
    return _("button", {
      type: "button",
      className: className,
      onMouseLeave: this.onDownButtonMouseLeave,
      onMouseDown: this.onDownButtonMouseDown,
      onMouseUp: this.onDownButtonMouseUp,
      onKeyDown: this.onDownButtonKeyDown,
      onKeyUp: this.onDownButtonKeyUp,
      disabled: this.props.disabled,
      tabIndex: "-1"
    }, _("span", {
      className: icon
    }));
  }

  renderButtonGroup() {
    const upButton = this.props.showButtons && this.renderUpButton();
    const downButton = this.props.showButtons && this.renderDownButton();

    if (this.isStacked()) {
      return _("span", {
        className: "p-inputnumber-button-group"
      }, upButton, downButton);
    }

    return _(React.Fragment, null, upButton, downButton);
  }

  render() {
    const className = classNames('p-inputnumber p-component', this.props.className, {
      'p-inputwrapper-filled': this.props.value != null,
      'p-inputwrapper-focus': this.focus,
      'p-inputnumber-buttons-stacked': this.isStacked(),
      'p-inputnumber-buttons-horizontal': this.isHorizontal(),
      'p-inputnumber-buttons-vertical': this.isVertical()
    });
    const inputElement = this.renderInputElement();
    const buttonGroup = this.renderButtonGroup();
    return _("span", {
      ref: el => this.element = el,
      id: this.props.id,
      className: className,
      style: this.props.style
    }, inputElement, buttonGroup);
  }

}

InputNumber.defaultProps = {
	value: null,
	format: true,
	showButtons: false,
	buttonLayout: 'stacked',
	incrementButtonClassName: null,
	decrementButtonClassName: null,
	incrementButtonIcon: 'pi pi-caret-up',
	decrementButtonIcon: 'pi pi-caret-down',
	locale: undefined,
	localeMatcher: undefined,
	mode: 'decimal',
	suffix: null,
	prefix: null,
	currency: undefined,
	currencyDisplay: undefined,
	useGrouping: true,
	minFractionDigits: undefined,
	maxFractionDigits: undefined,
	id: null,
	name: null,
	type: 'text',
	step: 1,
	min: null,
	max: null,
	disabled: false,
	required: false,
	tabIndex: null,
	pattern: null,
	inputMode: null,
	placeholder: null,
	readonly: false,
	size: null,
	style: null,
	className: null,
	inputId: null,
	inputStyle: null,
	inputClassName: null,
	tooltip: null,
	tooltipOptions: null,
	ariaLabelledBy: null,
	onChange: null,
	onBlur: null,
	onFocus: null
}

InputNumber.propTypes = {
	value: PropTypes.number,
	format: PropTypes.bool,
	showButtons: PropTypes.bool,
	buttonLayout: PropTypes.string,
	incrementButtonClassName: PropTypes.string,
	decrementButtonClassName: PropTypes.string,
	incrementButtonIcon: PropTypes.string,
	decrementButtonIcon: PropTypes.string,
	locale: PropTypes.string,
	localeMatcher: PropTypes.string,
	mode: PropTypes.string,
	suffix: PropTypes.string,
	prefix: PropTypes.string,
	currency: PropTypes.string,
	currencyDisplay: PropTypes.string,
	useGrouping: PropTypes.bool,
	minFractionDigits: PropTypes.number,
	maxFractionDigits: PropTypes.number,
	id: PropTypes.string,
	name: PropTypes.string,
	type: PropTypes.string,
	step: PropTypes.number,
	min: PropTypes.number,
	max: PropTypes.number,
	disabled: PropTypes.bool,
	required: PropTypes.bool,
	tabIndex: PropTypes.number,
	pattern: PropTypes.string,
	inputMode: PropTypes.string,
	placeholder: PropTypes.string,
	readonly: PropTypes.bool,
	size: PropTypes.number,
	style: PropTypes.object,
	className: PropTypes.string,
	inputId: PropTypes.string,
	inputStyle: PropTypes.object,
	inputClassName: PropTypes.string,
	tooltip: PropTypes.string,
	tooltipOptions: PropTypes.object,
	ariaLabelledBy: PropTypes.string,
	onChange: PropTypes.func,
	onBlur: PropTypes.func,
	onFocus: PropTypes.func
}

class InputSwitch extends React.Component {
  constructor(props) {
    super(props);
    this.state = {};
    this.onClick = this.onClick.bind(this);
    this.toggle = this.toggle.bind(this);
    this.onFocus = this.onFocus.bind(this);
    this.onBlur = this.onBlur.bind(this);
    this.onKeyDown = this.onKeyDown.bind(this);
  }

  onClick(event) {
    if (this.props.disabled) {
      return;
    }

    this.toggle(event);
    this.input.focus();
  }

  toggle(event) {
    if (this.props.onChange) {
      this.props.onChange({
        originalEvent: event,
        value: !this.props.checked,
        stopPropagation: () => {},
        preventDefault: () => {},
        target: {
          name: this.props.name,
          id: this.props.id,
          value: !this.props.checked
        }
      });
    }
  }

  onFocus(event) {
    this.setState({
      focused: true
    });

    if (this.props.onFocus) {
      this.props.onFocus(event);
    }
  }

  onBlur(event) {
    this.setState({
      focused: false
    });

    if (this.props.onBlur) {
      this.props.onBlur(event);
    }
  }

  onKeyDown(event) {
    if (event.key === 'Enter') {
      this.onClick(event);
    }
  }

  componentDidMount() {
    if (this.props.tooltip) {
      this.renderTooltip();
    }
  }

  componentDidUpdate(prevProps) {
    if (prevProps.tooltip !== this.props.tooltip) {
      if (this.tooltip) this.tooltip.updateContent(this.props.tooltip);else this.renderTooltip();
    }
  }

  componentWillUnmount() {
    if (this.tooltip) {
      this.tooltip.destroy();
      this.tooltip = null;
    }
  }

  renderTooltip() {
    this.tooltip = new Tooltip({
      target: this.container,
      content: this.props.tooltip,
      options: this.props.tooltipOptions
    });
  }

  render() {
    const className = classNames('p-inputswitch p-component', this.props.className, {
      'p-inputswitch-checked': this.props.checked,
      'p-disabled': this.props.disabled,
      'p-inputswitch-focus': this.state.focused
    });
    let inputSwitchProps = ObjectUtils.findDiffKeys(this.props, InputSwitch.defaultProps);
    return _("div", Object.assign({
      ref: el => this.container = el,
      id: this.props.id,
      className: className,
      style: this.props.style,
      onClick: this.onClick,
      role: "checkbox",
      "aria-checked": this.props.checked
    }, inputSwitchProps), _("div", {
      className: "p-hidden-accessible"
    }, _("input", {
      ref: el => this.input = el,
      type: "checkbox",
      id: this.props.inputId,
      name: this.props.name,
      checked: this.props.checked,
      onChange: this.toggle,
      onFocus: this.onFocus,
      onBlur: this.onBlur,
      onKeyDown: this.onKeyDown,
      disabled: this.props.disabled,
      role: "switch",
      "aria-checked": this.props.checked,
      "aria-labelledby": this.props.ariaLabelledBy
    })), _("span", {
      className: "p-inputswitch-slider"
    }));
  }

}

InputSwitch.defaultProps = {
	id: null,
	style: null,
	className: null,
	inputId: null,
	name: null,
	checked: false,
	disabled: false,
	tooltip: null,
	tooltipOptions: null,
	ariaLabelledBy: null,
	onChange: null,
	onFocus: null,
	onBlur: null
}

InputSwitch.propTypes = {
	id: PropTypes.string,
	style: PropTypes.object,
	className: PropTypes.string,
	inputId: PropTypes.string,
	name: PropTypes.string,
	checked: PropTypes.bool,
	disabled: PropTypes.bool,
	tooltip: PropTypes.string,
	tooltipOptions: PropTypes.object,
	ariaLabelledBy: PropTypes.string,
	onChange: PropTypes.func,
	onFocus: PropTypes.func,
	onBlur: PropTypes.func
}


class InputText extends React.Component {
  constructor(props) {
    super(props);
    this.onInput = this.onInput.bind(this);
    this.onKeyPress = this.onKeyPress.bind(this);
  }

  onKeyPress(event) {
    if (this.props.onKeyPress) {
      this.props.onKeyPress(event);
    }

    if (this.props.keyfilter) {
      KeyFilter.onKeyPress(event, this.props.keyfilter, this.props.validateOnly);
    }
  }

  onInput(event) {
    let validatePattern = true;

    if (this.props.keyfilter && this.props.validateOnly) {
      validatePattern = KeyFilter.validate(event, this.props.keyfilter);
    }

    if (this.props.onInput) {
      this.props.onInput(event, validatePattern);
    }

    if (!this.props.onChange) {
      if (event.target.value.length > 0) DomHandler.addClass(event.target, 'p-filled');else DomHandler.removeClass(event.target, 'p-filled');
    }
  }

  componentDidMount() {
    if (this.props.tooltip) {
      this.renderTooltip();
    }
  }

  componentDidUpdate(prevProps) {
    if (prevProps.tooltip !== this.props.tooltip) {
      if (this.tooltip) this.tooltip.updateContent(this.props.tooltip);else this.renderTooltip();
    }
  }

  componentWillUnmount() {
    if (this.tooltip) {
      this.tooltip.destroy();
      this.tooltip = null;
    }
  }

  renderTooltip() {
    this.tooltip = new Tooltip({
      target: this.element,
      content: this.props.tooltip,
      options: this.props.tooltipOptions
    });
  }

  render() {
    const className = classNames('p-inputtext p-component', this.props.className, {
      'p-disabled': this.props.disabled,
      'p-filled': this.props.value != null && this.props.value.toString().length > 0 || this.props.defaultValue != null && this.props.defaultValue.toString().length > 0
    });
    let inputProps = ObjectUtils.findDiffKeys(this.props, InputText.defaultProps);
    return _("input", Object.assign({
      ref: el => this.element = el
    }, inputProps, {
      className: className,
      onInput: this.onInput,
      onKeyPress: this.onKeyPress
    }));
  }

}

InputText.defaultProps = {
	onInput: null,
	onKeyPress: null,
	keyfilter: null,
	validateOnly: false,
	tooltip: null,
	tooltipOptions: null
};

InputText.propTypes = {
	onInput: PropTypes.func,
	onKeyPress: PropTypes.func,
	keyfilter: PropTypes.any,
	validateOnly: PropTypes.bool,
	tooltip: PropTypes.string,
	tooltipOptions: PropTypes.object
};


class InputTextarea extends React.Component {
  constructor(props) {
    super(props);
    this.onFocus = this.onFocus.bind(this);
    this.onBlur = this.onBlur.bind(this);
    this.onKeyUp = this.onKeyUp.bind(this);
    this.onInput = this.onInput.bind(this);
  }

  onFocus(e) {
    if (this.props.autoResize) {
      this.resize();
    }

    if (this.props.onFocus) {
      this.props.onFocus(e);
    }
  }

  onBlur(e) {
    if (this.props.autoResize) {
      this.resize();
    }

    if (this.props.onBlur) {
      this.props.onBlur(e);
    }
  }

  onKeyUp(e) {
    if (this.props.autoResize) {
      this.resize();
    }

    if (this.props.onKeyUp) {
      this.props.onKeyUp(e);
    }
  }

  onInput(e) {
    if (this.props.autoResize) {
      this.resize();
    }

    if (!this.props.onChange) {
      if (e.target.value.length > 0) DomHandler.addClass(e.target, 'p-filled');else DomHandler.removeClass(e.target, 'p-filled');
    }

    if (this.props.onInput) {
      this.props.onInput(e);
    }
  }

  resize() {
    if (DomHandler.isVisible(this.element)) {
      if (!this.cachedScrollHeight) {
        this.cachedScrollHeight = this.element.scrollHeight;
        this.element.style.overflow = "hidden";
      }

      if (this.cachedScrollHeight !== this.element.scrollHeight) {
        this.element.style.height = '';
        this.element.style.height = this.element.scrollHeight + 'px';

        if (parseFloat(this.element.style.height) >= parseFloat(this.element.style.maxHeight)) {
          this.element.style.overflowY = "scroll";
          this.element.style.height = this.element.style.maxHeight;
        } else {
          this.element.style.overflow = "hidden";
        }

        this.cachedScrollHeight = this.element.scrollHeight;
      }
    }
  }

  componentDidMount() {
    if (this.props.tooltip) {
      this.renderTooltip();
    }

    if (this.props.autoResize) {
      this.resize();
    }
  }

  componentDidUpdate(prevProps) {
    if (prevProps.tooltip !== this.props.tooltip) {
      if (this.tooltip) this.tooltip.updateContent(this.props.tooltip);else this.renderTooltip();
    }

    if (this.props.autoResize) {
      this.resize();
    }
  }

  componentWillUnmount() {
    if (this.tooltip) {
      this.tooltip.destroy();
      this.tooltip = null;
    }
  }

  renderTooltip() {
    this.tooltip = new Tooltip({
      target: this.element,
      content: this.props.tooltip,
      options: this.props.tooltipOptions
    });
  }

  render() {
    const className = classNames('p-inputtext p-inputtextarea p-component', this.props.className, {
      'p-disabled': this.props.disabled,
      'p-filled': this.props.value != null && this.props.value.toString().length > 0 || this.props.defaultValue != null && this.props.defaultValue.toString().length > 0,
      'p-inputtextarea-resizable': this.props.autoResize
    });
    let textareaProps = ObjectUtils.findDiffKeys(this.props, InputTextarea.defaultProps);
    return _("textarea", Object.assign({}, textareaProps, {
      className: className,
      ref: input => this.element = input,
      onFocus: this.onFocus,
      onBlur: this.onBlur,
      onKeyUp: this.onKeyUp,
      onInput: this.onInput
    }));
  }

}
InputTextarea.defaultProps = {
	autoResize: false,
	onInput: null,
	tooltip: null,
	tooltipOptions: null
};

InputTextarea.propTypes = {
	autoResize: PropTypes.bool,
	onInput: PropTypes.func,
	tooltip: PropTypes.string,
	tooltipOptions: PropTypes.object
};

class TabPanel extends React.Component {



}
TabPanel.defaultProps = {
    header: null,
    leftIcon: null,
    rightIcon: null,
    disabled: false,
    headerStyle: null,
    headerClassName: null,
    contentStyle: null,
    contentClassName: null
}

TabPanel.propTypes = {
    header: PropTypes.any,
    leftIcon: PropTypes.string,
    rightIcon: PropTypes.string,
    disabled: PropTypes.bool,
    headerStyle: PropTypes.object,
    headerClassName: PropTypes.string,
    contentStyle: PropTypes.object,
    contentClassName: PropTypes.string
};
"use strict";

class TabView extends React.Component {
  constructor(props) {
    super(props);

    if (!this.props.onTabChange) {
      this.state = {
        activeIndex: this.props.activeIndex
      };
    }

    this.id = this.props.id || UniqueComponentId();
  }

  isSelected(index) {
    const activeIndex = this.props.onTabChange ? this.props.activeIndex : this.state.activeIndex;
    return activeIndex === index;
  }

  onTabHeaderClick(event, tab, index) {
    if (!tab.props.disabled) {
      if (this.props.onTabChange) {
        this.props.onTabChange({
          originalEvent: event,
          index: index
        });
      } else {
        this.setState({
          activeIndex: index
        });
      }
    }

    event.preventDefault();
  }

  renderTabHeader(tab, index) {
    const selected = this.isSelected(index);
    const className = classNames(tab.props.headerClassName, 'p-unselectable-text', {
      'p-tabview-selected p-highlight': selected,
      'p-disabled': tab.props.disabled
    });
    const id = this.id + '_header_' + index;
    const ariaControls = this.id + '_content_' + index;
    const tabIndex = tab.props.disabled ? '-1' : null;
    return _("li", {
      className: className,
      style: tab.props.headerStyle,
      role: "presentation"
    }, _("a", {
      role: "tab",
      href: '#' + ariaControls,
      onClick: event => this.onTabHeaderClick(event, tab, index),
      id: id,
      "aria-controls": ariaControls,
      "aria-selected": selected,
      tabIndex: tabIndex
    }, tab.props.leftIcon && _("span", {
      className: classNames('p-tabview-left-icon ', tab.props.leftIcon)
    }), _("span", {
      className: "p-tabview-title"
    }, tab.props.header), tab.props.rightIcon && _("span", {
      className: classNames('p-tabview-right-icon ', tab.props.rightIcon)
    })));
  }

  renderTabHeaders() {
    return React.Children.map(this.props.children, (tab, index) => {
      return this.renderTabHeader(tab, index);
    });
  }

  renderNavigator() {
    const headers = this.renderTabHeaders();
    return _("ul", {
      className: "p-tabview-nav p-reset",
      role: "tablist"
    }, headers);
  }

  renderContent() {
    const contents = React.Children.map(this.props.children, (tab, index) => {
      if (!this.props.renderActiveOnly || this.isSelected(index)) {
        return this.createContent(tab, index);
      }
    });
    return _("div", {
      className: "p-tabview-panels"
    }, contents);
  }

  createContent(tab, index) {
    const selected = this.isSelected(index);
    const className = classNames(tab.props.contentClassName, 'p-tabview-panel', {
      'p-hidden': !selected
    });
    const id = this.id + '_content_' + index;
    const ariaLabelledBy = this.id + '_header_' + index;
    return _("div", {
      id: id,
      "aria-labelledby": ariaLabelledBy,
      "aria-hidden": !selected,
      className: className,
      style: tab.props.contentStyle,
      role: "tabpanel"
    }, !this.props.renderActiveOnly ? tab.props.children : selected && tab.props.children);
  }

  render() {
    const className = classNames('p-tabview p-component p-tabview-top', this.props.className);
    const navigator = this.renderNavigator();
    const content = this.renderContent();
    return _("div", {
      id: this.props.id,
      className: className,
      style: this.props.style
    }, navigator, content);
  }

}


TabView.defaultProps = {
    id: null,
    activeIndex: 0,
    style: null,
    className: null,
    renderActiveOnly: true,
    onTabChange: null
}

TabView.propTypes = {
    id: PropTypes.string,
    activeIndex: PropTypes.number,
    style: PropTypes.object,
    className: PropTypes.string,
    renderActiveOnly: PropTypes.bool,
    onTabChange: PropTypes.func
};