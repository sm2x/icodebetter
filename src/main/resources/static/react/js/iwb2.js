const dBGColors2 = [, , "#de9338", "#222", , , , , ];
let dgColors = ["warning", "secondary", "danger", "primary", "success", "info"];
let detailSpinnerColors2 = [
    "#187da0",
    "#2eadd3",
    "darkorange",
    "#187da0",
    "#4d6672",
    "#626a70",
    "#66767d"
];
let dgColors3 = [
    "gray-700",
    "danger",
    "gray-500",
    "gray-400",
    "gray-700",
    "info",
    "secondary",
    "secondary",
    "secondary",
    "warning"
];
// ReactRouterDOM for routing
const Link = ReactRouterDOM.Link;
const Route = ReactRouterDOM.Route;
const Switch = ReactRouterDOM.Switch;
const NavLink = ReactRouterDOM.NavLink;
const Redirect = ReactRouterDOM.Redirect;
const HashRouter = ReactRouterDOM.HashRouter;
// Reactstrap components
const Row = Reactstrap.Row;
const Col = Reactstrap.Col;
const Nav = Reactstrap.Nav;
const Card = Reactstrap.Card;
const Form = Reactstrap.Form;
const Alert = Reactstrap.Alert;
const Media = Reactstrap.Media;
const Input = Reactstrap.Input;
const Label = Reactstrap.Label;
const Table = Reactstrap.Table;
const Badge = Reactstrap.Badge;
const Modal = Reactstrap.Modal;
const Button = Reactstrap.Button;
const NavItem = Reactstrap.NavItem;

const Popover = Reactstrap.Popover;
const TabPane = Reactstrap.TabPane;
const Tooltip = Reactstrap.Tooltip;
const NavLinkS = Reactstrap.NavLink;
const FormText = Reactstrap.FormText;
const Dropdown = Reactstrap.Dropdown;
const CardBody = Reactstrap.CardBody;
const CardBlock = Reactstrap.CardBody;
const CardTitle = Reactstrap.CardTitle;
const ListGroup = Reactstrap.ListGroup;
const Container = Reactstrap.Container;
const ModalBody = Reactstrap.ModalBody;
const FormGroup = Reactstrap.FormGroup;
const CardHeader = Reactstrap.CardHeader;
const CardFooter = Reactstrap.CardFooter;
const Breadcrumb = Reactstrap.Breadcrumb;
const InputGroup = Reactstrap.InputGroup;
const Pagination = Reactstrap.Pagination;
const TabContent = Reactstrap.TabContent;
const PopoverBody = Reactstrap.PopoverBody;
const ModalHeader = Reactstrap.ModalHeader;
const ModalFooter = Reactstrap.ModalFooter;
const ButtonGroup = Reactstrap.ButtonGroup;
const NavbarBrand = Reactstrap.NavbarBrand;
const DropdownMenu = Reactstrap.DropdownMenu;
const DropdownItem = Reactstrap.DropdownItem;
const ListGroupItem = Reactstrap.ListGroupItem;
const NavbarToggler = Reactstrap.NavbarToggler;
const PopoverHeader = Reactstrap.PopoverHeader;
const DropdownToggle = Reactstrap.DropdownToggle;
const PaginationLink = Reactstrap.PaginationLink;
const PaginationItem = Reactstrap.PaginationItem;
const ButtonDropdown = Reactstrap.ButtonDropdown;
const BreadcrumbItem = Reactstrap.BreadcrumbItem;
const InputGroupAddon = Reactstrap.InputGroupAddon;
const InputGroupButton = Reactstrap.InputGroupButton;
const ListGroupItemText = Reactstrap.ListGroupItemText;
const UncontrolledTooltip = Reactstrap.UncontrolledTooltip;
const ListGroupItemHeading = Reactstrap.ListGroupItemHeading;
const CustomInput =  Reactstrap.CustomInput;

// FW Community Components
const Select = window.Select;
const Popper = window.Popper;
const findDOMNode = ReactDOM.findDOMNode;
// React
var _ = React.createElement;
// DXReactCore imports
const { DXReactCore, DXReactGrid, DXReactGridBootstrap4 } =
window.DevExpress !== undefined ? window.DevExpress : window;
const Getter = DXReactCore.Getter;
const Plugin = DXReactCore.Plugin;
const Template = DXReactCore.Template;
const TemplateConnector = DXReactCore.TemplateConnector;
const TemplatePlaceholder = DXReactCore.TemplatePlaceholder;

var _dxrg = DXReactGrid;
var _dxgrb = DXReactGridBootstrap4;


/**
 * @description iwb object is MIXIN like object most of the configuration is
 *              here and most used functions
 */
iwb = {
	dateFormat: 'DD/MM/YYYY',
    toastr: toastr,
    components: {},
    grids: {},
    label: {},
    forms: {},
    charts: {},
    formConversions: {},
    formSmsMailTemplates: {},
    formBaseValues(id) {
        var _smsStr = [],
            ss = iwb.formSmsMailTemplates[id];
        if (ss)
            for (var qi in ss)
                if (ss[qi]) _smsStr.push(qi);
        var _cnvStr = [],
            cs = iwb.formConversions[id];
        if (cs)
            for (var qi in cs)
                if (cs[qi]) _cnvStr.push(qi);
        return { _smsStr: _smsStr.join(","), _cnvStr: _cnvStr.join(",") };
    },
    loadGrid: {},
    tabs: {},
    closeTab: null,
    debug: false,
    debugRender: false,
    debugConstructor: false,
    detailPageSize: 10,
    log: console.log.bind(window.console),
    delayedTask : function (fnc) {
        var delayedTaskId = null;
        return {
            delay: (ms, params) => {
                if (delayedTaskId) clearTimeout(delayedTaskId);
                delayedTaskId = setTimeout(() => {
                    delayedTaskId = null;
                    fnc(params);
                }, ms);
            },
            stop: () => {
                if (delayedTaskId) clearTimeout(delayedTaskId);
                delayedTaskId = null;
            }
        }
    },
    loaders: {
        defaultWidth: 56,
        defaultHeight: 56,
        defaultFill: '#20a8d8',
        tailSpin: function(width, height, fill) {
            fill = fill || iwb.loaders.defaultFill;
            width = width || iwb.loaders.defaultWidth;
            height = height || iwb.loaders.defaultHeight || width;
            return _("svg", { width: width, height: height, viewBox: "0 0 38 38", xmlns: "http://www.w3.org/2000/svg" },
                _("defs", null, _("linearGradient", { x1: "8.042%", y1: "0%", x2: "65.682%", y2: "23.865%", id: "a" },
                    _("stop", { stopColor: fill, stopOpacity: "0", offset: "0%" }),
                    _("stop", { stopColor: fill, stopOpacity: ".631", offset: "63.146%" }),
                    _("stop", { stopColor: fill, offset: "100%" })
                )),
                _("g", { fill: "none", fillRule: "evenodd" },
                    _("g", { transform: "translate(1 1)" },
                        _("path", { d: "M36 18c0-9.94-8.06-18-18-18", id: "Oval-2", stroke: "url(#a)", strokeWidth: "2" },
                            _("animateTransform", {
                                attributeName: "transform",
                                type: "rotate",
                                from: "0 18 18",
                                to: "360 18 18",
                                dur: "0.9s",
                                repeatCount: "indefinite"
                            })
                        ),
                        _("circle", { fill: fill, cx: "36", cy: "18", r: "1" },
                            _("animateTransform", {
                                attributeName: "transform",
                                type: "rotate",
                                from: "0 18 18",
                                to: "360 18 18",
                                dur: "0.9s",
                                repeatCount: "indefinite"
                            })
                        )
                    )
                )
            );;
        },
        ballTriangle: function(width, height, fill) {
            fill = fill || iwb.loaders.defaultFill;
            width = width || iwb.loaders.defaultWidth;
            height = height || iwb.loaders.defaultHeight || width;
            return _("svg", { width: width, height: height, viewBox: "0 0 57 57", xmlns: "http://www.w3.org/2000/svg", stroke: fill || iwb.loaders.defaultFill },
                _("g", { fill: "none", fillRule: "evenodd" },
                    _("g", { transform: "translate(1 1)", strokeWidth: "2" },
                        _("circle", { cx: "5", cy: "50", r: "5" },
                            _("animate", {
                                attributeName: "cy",
                                begin: "0s",
                                dur: "2.2s",
                                values: "50;5;50;50",
                                calcMode: "linear",
                                repeatCount: "indefinite"
                            }),
                            _("animate", {
                                attributeName: "cx",
                                begin: "0s",
                                dur: "2.2s",
                                values: "5;27;49;5",
                                calcMode: "linear",
                                repeatCount: "indefinite"
                            })
                        ),
                        _("circle", { cx: "27", cy: "5", r: "5" },
                            _("animate", {
                                attributeName: "cy",
                                begin: "0s",
                                dur: "2.2s",
                                from: "5",
                                to: "5",
                                values: "5;50;50;5",
                                calcMode: "linear",
                                repeatCount: "indefinite"
                            }),
                            _("animate", {
                                attributeName: "cx",
                                begin: "0s",
                                dur: "2.2s",
                                from: "27",
                                to: "27",
                                values: "27;49;5;27",
                                calcMode: "linear",
                                repeatCount: "indefinite"
                            })
                        ),
                        _("circle", { cx: "49", cy: "50", r: "5" },
                            _("animate", {
                                attributeName: "cy",
                                begin: "0s",
                                dur: "2.2s",
                                values: "50;50;5;50",
                                calcMode: "linear",
                                repeatCount: "indefinite"
                            }),
                            _("animate", {
                                attributeName: "cx",
                                from: "49",
                                to: "49",
                                begin: "0s",
                                dur: "2.2s",
                                values: "49;5;27;49",
                                calcMode: "linear",
                                repeatCount: "indefinite"
                            })
                        )
                    )
                )
            );
        },
        puff: function(width, height, fill) {
            fill = fill || iwb.loaders.defaultFill;
            width = width || iwb.loaders.defaultWidth;
            height = height || iwb.loaders.defaultHeight || width;
            return _("svg", { width: width, height: height, viewBox: "0 0 44 44", xmlns: "http://www.w3.org/2000/svg", stroke: fill || iwb.loaders.defaultFill },
                _("g", { fill: "none", fillRule: "evenodd", strokeWidth: "2" },
                    _("circle", { cx: "22", cy: "22", r: "1" },
                        _("animate", {
                            attributeName: "r",
                            begin: "0s",
                            dur: "1.8s",
                            values: "1; 20",
                            calcMode: "spline",
                            keyTimes: "0; 1",
                            keySplines: "0.165, 0.84, 0.44, 1",
                            repeatCount: "indefinite"
                        }),
                        _("animate", {
                            attributeName: "stroke-opacity",
                            begin: "0s",
                            dur: "1.8s",
                            values: "1; 0",
                            calcMode: "spline",
                            keyTimes: "0; 1",
                            keySplines: "0.3, 0.61, 0.355, 1",
                            repeatCount: "indefinite"
                        })
                    ),
                    _("circle", { cx: "22", cy: "22", r: "1" },
                        _("animate", {
                            attributeName: "r",
                            begin: "-0.9s",
                            dur: "1.8s",
                            values: "1; 20",
                            calcMode: "spline",
                            keyTimes: "0; 1",
                            keySplines: "0.165, 0.84, 0.44, 1",
                            repeatCount: "indefinite"
                        }),
                        _("animate", {
                            attributeName: "stroke-opacity",
                            begin: "-0.9s",
                            dur: "1.8s",
                            values: "1; 0",
                            calcMode: "spline",
                            keyTimes: "0; 1",
                            keySplines: "0.3, 0.61, 0.355, 1",
                            repeatCount: "indefinite"
                        })
                    )
                )
            );
        }
    },
    mem: ((
        isArrayEqual = (array1, array2) =>
        array1.length === array2.length &&
        array1.every((value, index) => value === array2[index]) &&
        JSON.stringify(array1) === JSON.stringify(array2)
    ) => {
        let fnList = {},
            resultList = {},
            argList = {};
        return (resultFn, ...newArgs) => {
            let key =
                resultFn.toString().replace(/(\r\n\t|\n|\r\t|\s)/gm, "") +
                newArgs.toString().replace(/(,|\s)/gm, "");
            if (
                key &&
                fnList[key] &&
                resultList[key] &&
                isArrayEqual(argList[key], newArgs)
            ) {
                return resultList[key];
            }
            argList[key] = newArgs;
            resultList[key] = resultFn.apply(this, newArgs);
            fnList[key] = resultFn;
            return resultList[key];
        };
    })(),
    /**
     * A function to insert css classes into Dom
     *
     * @param {string}
     *            css - example '.aclass{display:none}'
     * @param {string}
     *            id - template id of the page not mandatory
     */
    addCssString: (css = "", id = Math.random()) => {
        let style = document.createElement("style");
        style.type = "text/css";
        style.id = "iwb-tpl-" + id;
        style.styleSheet ?
            (style.styleSheet.cssText = css) :
            style.appendChild(document.createTextNode(css));
        document["head"].appendChild(style);
    },
    /**
     * a function used for react.lazy
     * @param {string} url - example '/comp/2/js'
     */
    import: url => {
        var loc = document.location.href;
        var xloc = loc.split("main.htm");
        xloc[xloc.length - 1] = url;
        loc = xloc.join("");
        return new Promise((resolve, reject) => {
            if (Object.keys(iwb.components).indexOf(url) > 0) {
                resolve(iwb.components[url]);
                return iwb.components[url];
            }
            return fetch(loc)
                .then(res => res.text())
                .then(text => {
                    var result = new Function(text)();
                    iwb.components = Object.assign({},iwb.components, {[url]: result});//{...iwb.components, [url]: result };
                    resolve(result);
                });
        }).then(a => {
            a.default = a.default || a;
            return a;
        });
    },
    /**
     * @param {string}
     *            url - example '/comp/2/js'
     * @param {string}
     *            id - example '2' -id of the component
     */
    addCss: (url, id = Math.floor(Math.random() * 1000 + 1)) => {
        fetch(url)
            .then(response => response.text())
            .then(cssText => {
                if (document.getElementById(id) === null) {
                    let element = document.createElement("style");
                    element.innerHTML = cssText;
                    element.id = "style" + id;
                    window.document.head.appendChild(element);
                }
                return cssText;
            });
    },
    /**
     * @param {string}
     *            id - example '2' -id of the component
     */
    removePageCss: id => {
        let elem = document.getElementById("style" + id);
        if (elem !== null) {
            elem.parentNode.removeChild(elem);
        }
        return true;
    },
    loadable: loaderFunction =>
        class AsyncComponent extends React.Component {
            constructor(props) {
                super(props);
                this.state = { ResultComponent: null, error: false, errorText: "" };
            }
            componentWillMount() {
                loaderFunction
                    .then(result =>
                        this.setState({ ResultComponent: result.default || result })
                    )
                    .catch(errorText => this.setState({ error: true, errorText }));
            }
            render() {
                const { error, ResultComponent } = this.state;
                return ResultComponent ?
                    _(ResultComponent, Object.assign({},this.props)) :
                    error ?
                    _("span", { className: "alert alert-danger" }) :
                    _(XLoading, null);
            }
        },
    /**
     * @description used for giving data for grid button
     */
    commandComponentProps: {
        add: { icon: "plus", hint: "Create new row" },
        edit: { icon: "pencil", hint: "Edit row", color: "text-warning" },
        delete: { icon: "trash", hint: "Delete row", color: "text-danger" },
        cancel: { icon: "x", hint: "Cancel changes", color: "text-danger" },
        import: { icon: "target", hint: "Import" }
    },
    copyToClipboard: text => {
        const el = document.createElement("textarea");
        el.value = typeof text === "object" ? window.JSON.stringify(text) : text;
        el.style.position = "absolute";
        el.style.left = "-9999px";
        el.setAttribute("readonly", "");
        document.body.appendChild(el);
        el.select();
        document.execCommand("copy");
        document.body.removeChild(el);
        toastr.success("Use CTR + V to paste the content!", "Copied Successfully", {
            timeOut: 3000
        });
    },
  
    logo: '<img src="/images/rabbit-head.png" border=0 style="vertical-align: top;width: 28px;margin-top: -4px;">',

    detailSearch: () => false,
    fmtShortDate: x => {
        x ? moment(x).format(iwb.dateFormat) : "";
    },
    fmtDateTime: x => {
        x ? moment(x).format(iwb.dateFormat+" HH:mm") : "";
    },
    openForm: url => {
        if (url) iwb.openTab("1-" + Math.random(), url);
        return false;
    },
    sidebarToggle: e => {
        e.preventDefault();
        document.body.classList.toggle("sidebar-hidden");
    },
    sidebarMinimize: e => {
        e.preventDefault();
        document.body.classList.toggle("sidebar-minimized");
        document.body.classList.toggle("brand-minimized");
    },
    mobileSidebarToggle: e => {
        e.preventDefault();
        document.body.classList.toggle("sidebar-mobile-show");
    },
    asideToggle: e => {
        e.preventDefault();
        if (iwb.asideToggleX) iwb.asideToggleX(e);
        else document.body.classList.toggle("aside-menu-hidden");
    },
    /**
     * used to remove Global search value
     */
    killGlobalSearch: () => {
        iwb.onGlobalSearch2 = false;
        var component = document.getElementById("id-global-search");
        if (!component) return;
        component.value = "";
        component.classList.remove("global-search-active");
    },
    /**
     * Converts JSON to URI
     */
    JSON2URI: json => {
        if (!json) return "";
        var resultString = "";
        for (key in json)
            resultString +=
            encodeURIComponent(key) +
            "=" +
            (json[key] === null || json[key] === false ?
                "" :
                encodeURIComponent(json[key])) +
            "&";
        return resultString;
    },
    onGlobalSearch: v => {
        var component = document.getElementById("id-global-search");
        var cc = component.classList.contains("global-search-active");
        if ((c.value && !cc) || (!c.value && cc))
            c.classList.toggle("global-search-active");
        if (iwb.onGlobalSearch2) iwb.onGlobalSearch2(v);
    },
    getFieldRawValue: (field, extraOptions) => {
        if (!field || !field.value) return iwb.emptyField;
        if (field.$ === FileInput) {
            return field.fileName ?
                _(
                    "a", {
                        target: "_blank",
                        style: { color: "#2196F3", fontWeight: "bold" },
                        href: "dl/" + field.fileName + "?_fai=" + field.fileId,
                        className: "form-control",
                        disabled: true
                    },
                    field.fileName,
                    " ",
                    field.fileSize &&
                    _(
                        "i", { style: { color: "#888", fontWeight: "normal" } },
                        "(" + iwb.fmtFileSize(field.fileSize) + ")"
                    )
                ) :
                iwb.emptyField;
        }
        if (field.$ === MapInput)
            return _(field.$, { value: field.value, disabled: true });
        if (typeof XHTMLEditor!='undefined' && field.$ === XHTMLEditor)
            return _('div',{style:{border:'1px solid gray', borderRadius:2, padding:3}, dangerouslySetInnerHTML:{__html:field.value}});//_(field.$, { value: field.value, disabled: true });
        var options = extraOptions || field.options;
        if (!options || !options.length) {
            var value = field.decimalScale ?
                Number(field.value).toFixed(field.decimalScale) :
                field.value;
            if (typeof value == "undefined" || value == "") return iwb.emptyField;
            return _("b", { className: "form-control" }, value);
        }
        var optionsMap = {};
        options.map(o => {
            optionsMap[o.id] = o.dsc;
        });
        if (field.multi) {
            var value = [],
                vs = field.value;
            if (!Array.isArray(vs)) vs = vs.split(",");
            vs.map(v => {
                value.push(optionsMap[v]);
            });
            if (!value.length) return iwb.emptyField;
            return _("b", { className: "form-control" }, value.join(", "));
        }
        var value = field.value;
        if (value.id) value = value.id;
        value = optionsMap[value];
        if (value == undefined || value == "") return iwb.emptyField;
        return _("b", { className: "form-control" }, value);
    },
    approvalColorMap: {
        1: "primary",
        2: "warning",
        3: "danger",
        5: "success",
        901: "secondary",
        998: "success",
        999: "danger"
    },
    approvalLogs: arid => event => {
        event.preventDefault();
        iwb.ajax.query(
            1667, {
                xapproval_record_id: arid
            },
            j => {
                if (j.data && j.data.length)
                    iwb.showModal({
                        title: getLocMsg("workflow_logs"),
                        footer: false,
                        color: "primary",
                        size: "lg",
                        body: _(
                            "ul", { className: "timeline" },
                            j.data.map(item =>
                                _(
                                    "li", { className: "timeline-inverted" },
                                    _("div", {
                                        className: "timeline-badge bg-primary timeline-badge-icon"
                                    }),
                                    _(
                                        "div", { className: "timeline-panel" },
                                        _(
                                            "div", { className: "timeline-heading" },
                                            _(
                                                "h4", { className: "timeline-title" },
                                                _(
                                                    "span", {
                                                        className: "float-right badge badge-pill badge-" +
                                                            iwb.approvalColorMap[item.approval_action_tip]
                                                    },
                                                    item.approval_action_tip_qw_
                                                ),
                                                _("b", null, item.user_id_qw_),
                                                item.step_dsc
                                            ),
                                            _(
                                                "p", {},
                                                _(
                                                    "small", { className: "text-muted" },
                                                    _("i", { className: "icon-clock mx-1" }),
                                                    item.log_dttm
                                                )
                                            )
                                        ),
                                        _(
                                            "div", { className: "timeline-body" },
                                            item.comment && _("p", {}, item.comment)
                                        )
                                    )
                                )
                            )
                        )
                    });
                else toastr.info("no data to show", "warning");
            }
        );
    },
    request: cfg => {
        if (!window.fetch) {
            toastr.error("window.fetch not supported", "ERROR! ");
            return false;
        }
        if (!cfg || !cfg.url) {
            toastr.error("Config missing", "ERROR!");
            return false;
        }
        if (cfg.requestWaitMsg && iwb.loadingActive) iwb.loadingActive();
        fetch(cfg.url, {
                body: JSON.stringify(cfg.params || {}), // must match 'Content-Type'
                // header
                cache: "no-cache", // *default, no-cache, reload, force-cache,
                // only-if-cached
                credentials: "same-origin", // include, same-origin, *omit
                headers: {
                    "content-type": "application/json"
                },
                method: "POST", // *GET, POST, PUT, DELETE, etc.
                mode: "cors", // no-cors, cors, *same-origin
                redirect: "follow", // *manual, follow, error
                referrer: "no-referrer" // *client, no-referrer
            })
            .then(
                response => {
                    if (cfg.requestWaitMsg && iwb.loadingDeactive) iwb.loadingDeactive();

                    return response.status === 200 || response.status === 0 ?
                        response.json() :
                        Promise.reject(new Error(response.text() || response.statusText))
                })
            .then(
                result => {
                    if (cfg.callback && cfg.callback(result, cfg) === false) return;
                    if (result.success) {
                        if (cfg.successCallback) cfg.successCallback(result, cfg);
                    } else {
                        if (cfg.errorCallback && cfg.errorCallback(result, cfg) === false)
                            return;
                        iwb.requestErrorHandler(result);
                    }
                },
                error => {
                    if (
                        cfg.errorCallback &&
                        cfg.errorCallback({ error: error }, cfg) === false
                    )
                        return;
                    toastr.error(error || "Unknown ERROR", "Request Error");
                }
            );
    },
    requestErrorHandler: obj => {
        if (obj.errorType) {
            switch (obj.errorType) {
                case "session":
                    return iwb.showLoginDialog();
                case "validation":
                    toastr.error(obj.errors.join("<br/>"), "Validation Error",{ timeOut: 7000 });
                    break;
                case "sql":
                case "rhino":
                case "framework":
                case "cache":
                case "vcs":
                    if (obj.error || obj.objectType) {
                        iwb.showModal({
                            title: obj.objectType,
                            footer: false,
                            color: "danger",
                            size: "lg",
                            body: _(
                                Media, {
                                    body: true
                                },
                                obj.objectType &&
                                _(
                                    Media, {
                                        heading: true
                                    },
                                    obj.error || obj.objectType
                                ),

                                _(
                                    ListGroup, {},
                                    obj.icodebetter &&
                                    obj.icodebetter.map((item, index) => {
                                        return _(
                                            ListGroupItem, {},
                                            item &&
                                            _(
                                                ListGroupItemText, {},
                                                _(
                                                    "b", {},
                                                    item.objectType || item.errorType || "x"
                                                ),
                                                ": " + item.error,
                                                _(
                                                    Button, {
                                                        className: "float-right btn btn-xs",
                                                        color: "info",
                                                        onClick: e => {
                                                            e.preventDefault();
                                                            iwb.copyToClipboard(item);
                                                        }
                                                    },
                                                    _(
                                                        "i", {
                                                            className: "icon-docs"
                                                        },
                                                        ""
                                                    )
                                                ),
                                                _(
                                                    Button, {
                                                        className: "float-right btn btn-xs",
                                                        color: "primary",
                                                        onClick: e => {
                                                            e.preventDefault();
                                                            iwb.log(item);
                                                            toastr.success(
                                                                "Use CTR + SHIFT + I to see the log content!",
                                                                "Console Log", { timeOut: 3000 }
                                                            );
                                                        }
                                                    },
                                                    _(
                                                        "i", {
                                                            className: "icon-target"
                                                        },
                                                        ""
                                                    )
                                                )
                                            )
                                        );
                                    })
                                )
                            )
                        });
                    }
                    break;
                default:
                    toastr.error(
                        obj.error || obj.errorMsg || "Unknown ERROR",
                        obj.errorType + " Error"
                    );
            }
        } else {
            toastr.error(obj.errorMsg || "Unknown ERROR", "Request Error");
        }
    },
    getFormValues: formObj => {
        if (!formObj || !formObj.elements) return {};
        var elements = formObj.elements,
            values = {};
        for (var index = 0; index < elements.length; index++) {
            if (elements[index].name)
                switch (elements[index].type) {
                    case "checkbox":
                        values[elements[index].name] = elements[index].checked;
                        break;
                    case "hidden":
                        values[elements[index].name] =
                            values[elements[index].name] === undefined ?
                            elements[index].value :
                            values[elements[index].name] + "," + elements[index].value;
                        break;
                    default:
                        values[elements[index].name] = elements[index].value;
                }
        }
        return values;
    },
    /**
     * @description sadece master-insert durumunda cagir. farki _postMap ve hic
     *              bir zaman _insertedItems,_deletedItems dikkate almamasi
     * @param {*}
     *            grid
     * @param {*}
     *            prefix
     * @param {*}
     *            values
     */
    prepareParams4grid: (grid, prefix, values) => {
        var dirtyCount = 0;
        var params = {};
        var items = values.deleted;
        var pk = grid._pk || grid.pk;
        if (items)
            for (var bjk = 0; bjk < items.length; bjk++) {
                // deleted
                dirtyCount++;
                for (var key in pk) {
                    var val = pk[key];
                    if (typeof val == "function") {
                        params[key + prefix + "." + dirtyCount] = val(items[bjk]);
                    } else {
                        params[key + prefix + "." + dirtyCount] =
                            val.charAt(0) == "!" ? val.substring(1) : items[bjk][val];
                    }
                }
                params["a" + prefix + "." + dirtyCount] = 3;
            }
        items = values.changed;
        if (items)
            for (var bjk = 0; bjk < items.length; bjk++) {
                // edited
                dirtyCount++;
                params["a" + prefix + "." + dirtyCount] = 1;
                var changes = items[bjk]._new;
                for (var key in changes)
                    params[key + prefix + "." + dirtyCount] = changes[key];
                if (grid._postMap)
                    for (var key in grid._postMap) {
                        var val = grid._postMap[key];
                        if (typeof val == "function") {
                            params[key + prefix + "." + dirtyCount] = val(changes);
                        } else {
                            params[key + prefix + "." + dirtyCount] =
                                val.charAt(0) == "!" ? val.substring(1) : changes[val];
                        }
                    }

                for (var key in pk) {
                    var val = pk[key];
                    if (typeof val == "function") {
                        params[key + prefix + "." + dirtyCount] = val(items[bjk]);
                    } else {
                        params[key + prefix + "." + dirtyCount] =
                            val.charAt(0) == "!" ? val.substring(1) : items[bjk][val];
                    }
                }
            }
        items = values.inserted;
        if (items)
            for (var bjk = 0; bjk < items.length; bjk++) {
                dirtyCount++;
                params["a" + prefix + "." + dirtyCount] = 2;
                var changes = items[bjk]._new;
                for (var key in changes)
                    params[key + prefix + "." + dirtyCount] = changes[key];

                if (grid._postMap)
                    for (var key in grid._postMap) {
                        var val = grid._postMap[key];
                        if (typeof val == "function") {
                            params[key + prefix + "." + dirtyCount] = val(changes);
                        } else {
                            params[key + prefix + "." + dirtyCount] =
                                val.charAt(0) == "!" ? val.substring(1) : changes[val];
                        }
                    }
                if (grid._postInsertParams) {
                    for (var key in grid._postInsertParams)
                        params[key + prefix + "." + dirtyCount] =
                        grid._postInsertParams[key];
                }
            }
        if (dirtyCount > 0) {
            params["_cnt" + prefix] = dirtyCount;
            params["_fid" + prefix] = grid.crudFormId;
            return params;
        } else return {};
    }
};
var ajaxErrorHandler = iwb.requestErrorHandler;
(iwb.emptyField = _(
    "i", { className: "raw-field-empty" },
    _("br"),
    " ",
    "(empty)"
)),
(iwb.loadPage = function(cfg) {});
iwb.ui = {
    buildPanel: c => {
        if (c.grid) {
            if (!c.grid.pk) c.grid.pk = c.pk || c._pk;
            if (!c.grid.detailGrids) c.grid.detailGrids = c.detailGrids || false;
            return _(XPage, c);
        } else if (c.card) {
            if (!c.card.pk) c.card.pk = c.pk || c._pk;
            if (!c.card.detailGrids) c.card.detailGrids = c.detailGrids || false;
            return _(XPage4Card, c);
        }
    }
};

function disabledCheckBoxHtml(row, cell) {
    return row[cell] && 1 * row[cell] ?
        _("i", {
            className: "fa fa-check",
            style: {
                color: "#44b848",//border:"1px solid #a3dca5",
//                background: "#aeeac3",
                padding: 5, fontSize:14,
//                borderRadius: 25
            }
        }) :
        null; // _('i',{className:'fa fa-check', style:{color: 'white',background:
    // 'red', padding: 5, borderRadius: 25}});
}

function gridUserRenderer(row, cell) {
    // TODO
    return row[cell + "_qw_"];
}

function gridQwRendererWithLink(t) {
    // tableId
    return function(row, cell) {
        return row[cell + "_qw_"];
    };
}

function editGridComboRenderer(cell, combo) {
    if (!combo || !combo.options)
        return function(row) {
            return "?x?: " + row[cell];
        };
    var moptions = {};
    combo.options.map(function(o) {
        moptions[o.id] = o;
    });
    combo.moptions = moptions;
    return function(row) {
        var c = row[cell];
        if (!c) return "";
        var o = combo.moptions[c];
        return o ? o.dsc : "???: " + row[cell];
    };
}

function editGridLovComboRenderer(cell, combo) {
    if (!combo || !combo.options)
        return function(row) {
            return "?x?: " + row[cell];
        };
    var moptions = {};
    combo.options.map(option => {
        moptions[option.id] = option;
    });
    combo.moptions = moptions;
    return row => {
        var tempCell = row[cell];
        if (!tempCell) return "";
        tempCell = tempCell.split(",");
        return tempCell.map(opName => combo.moptions[opName]);
    };
}

function fileAttachmentHtml(row, cell) {
    // TODO
    return row[cell] && 1 * row[cell] ?
        _("i", { className: "icon-paper-clip" }) :
        null;
}

function fieldFileAttachment(row, cell) {
    return row[cell] && 1 * row[cell] ?
        _(
            "a", {
                href: "dl/" + row[cell + "_qw_"] + "?_fai=" + row[cell],
                target: "_blank"
            },
            row[cell + "_qw_"]
        ) :
        null;
}

function vcsHtml(row, cell) {
    return row[cell] && 1 * row[cell] ?
        _("i", { className: "icon-social-github" }) :
        null;
}

function pictureHtml(row, cell) {
    return row[cell] && 1 * row[cell] ?
        _("i", { className: "icon-picture" }) :
        null;
}

function mailBoxRenderer(row, cell) {
    return row[cell] && 1 * row[cell] ?
        _("i", { className: "icon-envelope" }) :
        null;
}

function strShortDate(row, cell) {
    return cell && row ? (row[cell] ? row[cell].substr(0, 10) : "") : (row ? row.substr(0, 10) : "");
}

function accessControlHtml() {
    return null;
}

function fmtDateTime(x, y) {
    return x ? moment(x).format(iwb.dateFormat + " HH:mm") : "";
}

function fmtShortDate(x, y) {
    return x ? moment(x).format(iwb.dateFormat) : "";
}

function strDateTime(row, cell) {
    return cell && row ? (row[cell] ? row[cell] : "") : (row ? row : "");
}


var daysOfTheWeek = {
    tr: [
        "Pazar",
        "Pazartesi",
        "Salı",
        "Çarşamba",
        "Perşembe",
        "Cuma",
        "Cumartesi"
    ],
    en: [
        "Sunday",
        "Monday",
        "Tuesday",
        "Wednesday",
        "Thursday",
        "Friday",
        "Saturday"
    ]
};
var xtimeMap = {
    tr: ["az önce", "bir dakika önce", "dakika önce", "saat önce", "dün"],
    en: ["seconds ago", "a minute ago", "minutes ago", "hours ago", "yesterday"]
};

function fmtDateTimeAgo(dt2) {
    if (!dt2) return "";
    var tnow = new Date().getTime();
    var dt3 = moment(dt2, iwb.dateFormat + " HH:mm").toDate();
    var t = dt3.getTime();
    var xt = xtimeMap[_scd.locale] || {};
    if (t + 30 * 1000 > tnow) return xt[0]; // 'Az Önce';//5 sn
    if (t + 2 * 60 * 1000 > tnow) return xt[1]; // 'Bir Dakika Önce';//1 dka
    if (t + 60 * 60 * 1000 > tnow)
        return Math.round((tnow - t) / (60 * 1000)) + xt[2]; // ' Dakika Önce';
    if (t + 24 * 60 * 60 * 1000 > tnow)
        return Math.round((tnow - t) / (60 * 60 * 1000)) + xt[3]; // ' Saat Önce';
    if (t + 2 * 24 * 60 * 60 * 1000 > tnow) return xt[4]; // 'Dün';
    if (t + 7 * 24 * 60 * 60 * 1000 > tnow)
        return daysOfTheWeek[_scd.locale][dt3.getDay()]; // 5dka
    return dt2.substr(0, 10);
}

function strDateTimeAgo(row, cell) {
    return cell && row ? (row[cell] ? fmtDateTimeAgo(row[cell]) : "") : (row ? fmtDateTimeAgo(row) : "");
}

function getStrapSize(w) {
    if (w >= 700) return "lg";
    if (w >= 400) return "md";
    return "sm";
}

function getMasterGridSel(a, sel) {
    return sel;
}

function buildParams2(params, map) {
    var bp = "";
    for (var key in params) {
        var newKey = params[key];
        if (typeof newKey == "function") {
            bp += "&" + key + "=" + newKey(params);
        } else if (newKey.charAt(0) == "!")
            bp += "&" + key + "=" + newKey.substring(1);
        else bp += "&" + key + "=" + map[params[key]];
    }
    return bp;
}
/**
 * @description Grids common methods are located in this class
 */
class GridCommon extends React.PureComponent {
        constructor(props) {
            super(props);
            this.state = {};
            this.lastQuery;
            /**
             * @description Used to set State of Grid with pagination number
             * @param {Number}
             *            currentPage - current page number
             */
            this.onCurrentPageChange = currentPage => this.setState({ currentPage });
            /**
             * @description Used to Set State of grid with Column width
             * @param {String}
             *            columnWidths[].columnName - name of the column
             * @param {Number}
             *            columnWidths[].width - width of the column
             */
            this.onColumnWidthsChange = columnWidths => this.setState({ columnWidths });
            /**
             * @description Used to Set State of Grid with column order
             * @param {Array}
             *            order - ["ColName1","ColName2",...]
             */
            this.onOrderChange = order => this.setState({ order });
            /**
             * @description Used to set Pagination row Nummber
             * @param {Number}
             *            pageSize - sets size of the number for
             */
            this.onPageSizeChange = pageSize => {
                var { currentPage, totalCount } = this.state;
                currentPage = Math.min(currentPage, Math.ceil(totalCount / pageSize) - 1);
                this.setState({ pageSize, currentPage });
            };
            /**
             * @description get selected array from grid
             */
            this.getSelected = () =>
                this.state.rows.reduce((accumulator, row) => {
                    this.state.selection.includes(row[props.keyField]) ?
                        accumulator.push(row) :
                        "";
                    return accumulator;
                }, []);
            // //////////////////////////////////////////------2-----////////////////////////////////////////
            /**
             * @description Used to Set Sorting state of Grid with column name
             * @example Used only in XMainGrid and XGrid
             * @param {String}
             *            sorting
             */
            this.onSortingChange = sorting => this.setState({ sorting });
            /**
             * @description
             *
             * You can access every row data Also Used to Map Doulble click action to
             * the row
             * @example Used Only in XMainGrid and XGrid
             * @param {Object}
             *            tableRowData -
             * @param {Symbol}
             *            tableRowData.childeren -React.Components
             * @param {Object}
             *            tableRowData.row - Row Data
             * @param {Object}
             *            tableRowData.TableRow - {Current RowData,key,type,rowId}
             */
            this.rowComponent = tableRowData => {
                var { openTab, crudFlags, pk, crudFormId } = this.props;
                return _(
                    _dxgrb.Table.Row,
                    openTab && crudFlags && crudFlags.edit && pk && crudFormId ?
                    Object.assign({},tableRowData,
                        {
                            onDoubleClick: event =>
                                this.onEditClick({
                                    event,
                                    rowData: tableRowData.row,
                                    openEditable: !!this.props.openEditable
                                }),
                            style: Object.assign({},tableRowData.style, {cursor: "pointer" })
                        }
                    ) :
                    tableRowData
                );
            };
            /**
             * @description will open new page with
             * @example Used in XMainGrid and XGrid
             * @param {event}
             *            event - Event from of the clicked buttuon
             * @param {state/props}
             *            grid -state of the grid
             * @param {Array}
             *            row - row data to pass into _postInsert
             */
            this.onOnNewRecord = (event, grid, row) => {
                if (!grid) grid = this.props;
                if (grid.crudFlags && grid.crudFlags.insert && this.props.openTab) {
                    var url = "showForm?a=2&_fid=" + grid.crudFormId;
                    if (grid._postInsert) {
                        url = grid._postInsert(row || {}, url, grid);
                        if (!url) return;
                    }
                    var modal = !!event.ctrlKey;
                    this.props.openTab(
                        "2-" + grid.gridId,
                        url + (modal ? "&_modal=1" : ""), {}, { modal: modal }
                    );
                }
            };
            /**
             * @description prerpares url with query
             * @example Used in XMainGrid and XGrid to make query inside loadData
             * @returns {String}
             */
            this.queryString = () => {
                const { sorting, pageSize, currentPage } = this.state;
                let queryString =
                    this.props._url +
                    "&limit=" +
                    pageSize +
                    "&start=" +
                    pageSize * currentPage;
                const columnSorting = sorting[0];
                if (columnSorting) {
                    const sortingDirectionString =
                        columnSorting.direction === "desc" ? " desc" : "";
                    queryString +=
                        "&sort=" + columnSorting.columnName + sortingDirectionString;
                }
                return queryString;
            };
            /**
             * @description Used to Edit and double click on the row
             * @param {
             *            Object } param0 - consist of two data evet and Rowdata
             * @param {
             *            Event } param0.event - Click event from the Edit button and
             *            double click on the row
             * @param {
             *            rowData } param0.rowData - Data of the row where the Edit
             *            button or double click clicked
             */
            this.onEditClick = ( extraProps ) => {
            	var { event, rowData, openEditable} = extraProps;
                var { props } = this;
                var pkz = buildParams2(props.pk, rowData);
                var url = "showForm?a=1&_fid=" + props.crudFormId + pkz;
                if (props._postUpdate) {
                    var url = this.props._postUpdate(rowData, url, props);
                    if (!url) return;
                }
                var modal = event.ctrlKey && !!event.ctrlKey;
                props.openTab(
                    "1-" + pkz,
                    url + (modal ? "&_modal=1" : ""), {}, Object.assign({},{ modal, openEditable, rowData}, props, extraProps )
                );
            };
            /**
             * todo
             *
             * @param {Object}
             *            param0 - event from delete button
             * @param {Event}
             *            param0.event - event from delete button
             * @param {Array}
             *            param0.rowData - data for the deleted Row
             */
            this.onDeleteClick = ({ event, rowData }) => {
                var { pk, crudFormId } = this.props;
                var pkz = buildParams2(pk, rowData);
                var url = "ajaxPostForm?a=3&_fid=" + crudFormId + pkz;
                yesNoDialog({
                    text: getLocMsg("are_you_sure"),
                    callback: success => {
                        if (success) {
                            iwb.request({ url, successCallback: () => this.loadData(true) });
                        }
                    }
                });
            };
            /**
             * @description used to make request and fill the grid
             * @param {boolean}
             *            force - to fill with up to date data
             */
            this.loadData = force => {
                if (this.props.rows || this.state.loading) return;
                const queryString = this.queryString();
                if (!force && queryString === this.lastQuery) {
                    return;
                }
                this.setState({ rows: [], loading: true });
                iwb.request({
                    url: queryString,
                    self: this,
                    params: this.props.searchForm &&
                        iwb.getFormValues(document.getElementById(this.props.searchForm.id)),
                    successCallback: (result, cfg) => {
                        cfg.self.setState({
                            rows: result.data,
                            totalCount: result.total_count,
                            loading: false
                        });
                    },
                    errorCallback: (error, cfg) => {
                        cfg.self.setState({
                            rows: [],
                            totalCount: 0,
                            loading: false
                        });
                    }
                });
                this.lastQuery = queryString;
            };
            // ####################################EDit Grid Common
            // ############################################
            /**
             * @param {Array}
             *            editingRowIds - IDs of the Editing rows
             */
            this.onEditingRowIdsChange = editingRowIds =>
                this.setState({ editingRowIds });
            /**
             * @description A function that returns a row change object depending on row
             *              editor values. This function is called each time the row
             *              editor’s value changes.
             * @param {object}
             *            addedRows - (row: any, columnName: string, value: string |
             *            number)
             */
            this.onAddedRowsChange = addedRows => {
                var newRecord = Object.assign({}, this.props.newRecord || {});
                var pk = this.state.pkInsert;
                --pk;
                newRecord[this.props.keyField] = pk;
                this.setState({
                    pk4Insert: pk,
                    addedRows: addedRows.map(
                        row => (Object.keys(row).length ? row : newRecord)
                    )
                });
            };
            /**
             * @description Handles adding or removing a row changes to/from the
             *              rowChanges array.
             * @param {Array}
             *            rowChanges -(rowChanges: { [key: string]: any }) => void
             */
            this.onRowChangesChange = rowChanges => {
                this.setState({ rowChanges });
            };
            /**
             * @description Handles selection changes.
             * @param {Array}
             *            selection - (selection: Array<number | string>) => void
             */
            this.onSelectionChange = (selection,aqq) => {

                this.setState({ selection });
            	if(this.props.onSelectionChange){
            		var sels = this.state.rows.reduce((accumulator, row) => {
            			selection.includes(row[this.props.keyField]) ?
                            accumulator.push(row) :
                            "";
                        return accumulator;
                    }, []);
            		this.props.onSelectionChange(sels);
            	}
            };
            /**
             * Used to delete from the frontend
             *
             * @param {Array}
             *            param0
             * @param {Array}
             *            param0.deleted
             */
            this.onCommitChanges = ({ deleted }) => {
                let { rows, deletedRows } = this.state;
                if (deleted && deleted.length) {
                    yesNoDialog({
                        text: getLocMsg("are_you_sure"),
                        callback: success => {
                            if (success) {
                                rows = rows.slice();
                                deleted.forEach(rowId => {
                                    const index = rows.findIndex(
                                        row => row[this.props.keyField] === rowId
                                    );
                                    if (index > -1) {
                                        if (rowId > 0) {
                                            deletedRows.push(Object.assign({}, rows[index]));
                                        }
                                        rows.splice(index, 1);
                                    }
                                });
                                this.setState({ rows, deletingRows: [], deletedRows });
                            }
                        }
                    });
                }
            };
            /**
             * @example push id to this.state.deletingRows then this.deleteRows();
             */
            this.deleteRows = () => {
                const rows = this.state.rows.slice();
                this.state.deletingRows.forEach(rowId => {
                    const index = rows.findIndex(row => row.id === rowId);
                    if (index > -1) {
                        rows.splice(index, 1);
                    }
                });
                this.setState({ rows, deletingRows: [] });
            };
        }
    }
    /**
     * helper componet for MapInput
     */
class XMap extends React.PureComponent {
    constructor(props) {
            super(props);
            /**
             * there is no state since if we provide state it will start rerendering
             * itself
             */
            this.map;
            this.script;
            this.marker;
            this.geocoder;
            this.inputNode;
            this.infoWindow;
            this.autoComplete;
            this.elementsWithListeners = [];
            this.defPosition = { lat: 41.0082, lng: 28.9784 };
            !props.apiKey && alert("GoogleMaps:::::::this.props.apiKey not provided");
            this.id = "GoogleMaps" + Math.floor(Math.random() * 1000 + 1);
            /*
             * runs on ofter scrip is loaded
             */
            this.onScriptLoad = () => {
                this.map = this.createMap(this.props.mapOpt || {});
                this.marker = this.createMarker(this.props.markerOpt || {});
                this.geocoder = this.createGeocoder(this.props.geocoderOpt || {});
                this.infoWindow = this.createInfoWindow(Object.assign({},{
                    maxWidth: 300},
                    this.props.infoWindowOpt
                ));
                this.autoComplete = this.createAutocomplete(
                    this.props.autocompleteOpt || undefined
                );
                this.props.onMapLoad && this.props.onMapLoad(this);
                /** after all the map listeners is set */
                this.elementsWithListeners.push(this.map);
                this.elementsWithListeners.push(this.marker);
                this.elementsWithListeners.push(this.script);
                this.elementsWithListeners.push(this.geocoder);
                this.elementsWithListeners.push(this.inputNode);
                this.elementsWithListeners.push(this.infoWindow);
                this.elementsWithListeners.push(this.autoComplete);
            };
            /**
             * locate me on the map and used as if stetement for displaying button
             * of geolocation
             */
            this.findMe = () => {
                if (window.navigator.geolocation) {
                    window.navigator.geolocation.getCurrentPosition(this.findMeOuter);
                    return true;
                } else {
                    return false;
                }
            };
            /**
             * A function to create Google Map object
             *
             * @param {Object}
             *            opt
             */
            this.createMap = opt => {
                let opt1 = {
                    center: this.defPosition,
                    zoom: 8
                };
                return new window.google.maps.Map(document.getElementById(this.id), Object.assign({},opt1,opt));
            };
            /**
             * A function return GMarker
             */
            this.createMarker = opt => {
                let opt1 = {
                    position: this.defPosition,
                    draggable: true,
                    map: this.map,
                    title: "default title"
                };
                return new window.google.maps.Marker(Object.assign({},opt1, opt ));
            };
            /**
             * a function used to init geolocation
             *
             * @param {Object}
             *            opt
             */
            this.createGeocoder = opt => {
                let opt1 = {};
                return new window.google.maps.Geocoder(Object.assign({},opt1, opt ));
            };
            /**
             * a function to create InfoWindow
             *
             * @param {Object}
             *            opt
             */
            this.createInfoWindow = opt => {
                let opt1 = {
                    content: `<div id="infoWindow" />`,
                    position: this.defPosition
                };
                return new window.google.maps.InfoWindow(Object.assign({},opt1, opt ));
            };
            /**
             * A function return Autocomplete
             *
             * @param {HTMLElement}
             *            inputNode
             */
            this.createAutocomplete = (
                inputNode = document.getElementById("pac-input")
            ) => {
                this.inputNode = inputNode;
                return new window.google.maps.places.Autocomplete(inputNode);
            };
            /**
             * function to remove listeners from the dom element
             *
             * @param {HTMLElement}
             *            element
             */
            this.removeAllEventListenersFromElement = element => {
                /**
                 * to find out if it is a dom object
                 */
                if (element && element.cloneNode) {
                    let clone = element.cloneNode();
                    // move all child elements from the original to the clone
                    while (element.firstChild) {
                        clone.appendChild(element.lastChild);
                    }
                    element.parentNode.replaceChild(clone, element);
                }
            };
            /**
             * A function to remove listeners from the array of obj
             *
             * @param {Array}
             *            elements
             */
            this.removeAllEventListenersFromElements = (elements = []) => {
                /** cheks if it is array */
                elements &&
                    typeof elements.length === "number" &&
                    elements.length > 0 &&
                    elements.map(this.removeAllEventListenersFromElement);
            };
        }
        /**
         * Used to load script to the body
         */
    componentDidMount() {
            if (!window.google) {
                this.script = document.createElement("script");
                this.script.id = "script-" + this.id;
                this.script.type = "text/javascript";
                this.script.async = false;
                this.script.src = `https://maps.googleapis.com/maps/api/js?key=${
        this.props.apiKey
      }&libraries=places`;
                var xscript = document.getElementsByTagName("script")[0];
                xscript.parentNode.insertBefore(this.script, xscript);
                // Below is important.
                // We cannot access google.maps until it's finished loading
                this.script.addEventListener("load", e => {
                    this.onScriptLoad();
                });
            } else {
                this.onScriptLoad();
            }
        }
        /**
         * Used to delete all listeners and delete script from the body but all
         * the google func will work
         */
    componentWillUnmount() {
        this.removeAllEventListenersFromElements(this.elementsWithListeners);
    }
    render() {
        return React.createElement(
            React.Fragment,
            null,
            React.createElement(
                PopoverHeader,
                null,
                React.createElement(
                    FormGroup,
                    null,
                    React.createElement(Label, { for: "exampleEmail" }, "Address"),
                    React.createElement(
                        "div", {
                            style: { cursor: "pointer" },
                            className: "float-right",
                            onClick: () => {
                                this.props.onClick(false);
                            }
                        },
                        React.createElement("i", { className: "icon-close" })
                    ),
                    React.createElement(
                        InputGroup, { type: "text", name: "name" },
                        React.createElement(
                            InputGroupAddon, { hidden: !!this.findMe, addonType: "prepend" },
                            React.createElement(
                                Button, {
                                    disabled: !!this.findMe,
                                    type: "submit",
                                    onClick: this.findMe
                                },
                                React.createElement("i", { className: "icon-location-pin" })
                            )
                        ),
                        React.createElement(Input, {
                            id: "pac-input",
                            type: "text",
                            placeholder: "Enter a location"
                        }),
                        React.createElement(
                            InputGroupAddon, { addonType: "append" },
                            React.createElement(
                                Button, {
                                    type: "submit",
                                    onClick: this.props.onClick,
                                    className: "btn btn-success"
                                },
                                _("i", { className: "icon-pin" })
                            )
                        )
                    )
                )
            ),
            React.createElement(
                PopoverBody,
                null,
                React.createElement("div", {
                    style: {
                        width: this.props.width || 400,
                        height: this.props.height || 400
                    },
                    id: this.id
                })
            )
        );
    }
}
class MapInput extends React.PureComponent {
        constructor(props) {
            super(props);
            let st =
                props.stringifyResult && props.value ?
                JSON.parse(props.value) :
                props.value;
            this.state = {
                zoom: st.zoom || 8,
                maptype: st.maptype || "roadmap",
                formatted_address: st.formatted_address || "",
                place_id: st.place_id || "",
                place_lat: st.place_lat || "",
                place_lng: st.place_lng || "",
                mapOpen: false
            };
            this.popoverId = this.props.id ?
                "popoverId" + this.props.id :
                "popoverId" + Math.floor(Math.random() * 1000 + 1);
            /**
             * a function used to hide and open the map on the DOM
             */
            this.toggle = () => {
                this.setState(prevState => ({
                    mapOpen: !prevState.mapOpen
                }));
            };
            /**
             * a function used to render info window content
             */
            this.getInfoWindowContent = () => {
                return `
	            <div class="">
	                <div class="card-body">
	                    <h5 class="card-title text-center">
	                    		<i class="navbar-brand icon-globe"></i>
	                    ${this.state.formatted_address}</h5>
	                </div>
	            </div>
	            `;
            };
            /**
             * it is a callback function which will work after imporing the google
             * script
             *
             * @param {object}
             *            innerScope - state of the internal component
             */
            this.onMapLoad = innerScope => {
                innerScope.geocoder.geocode(
                    this.state.place_id ?
                    { placeId: this.state.place_id } :
                    { latLng: innerScope.defPosition || undefined },
                    (result, status) => {
                        if (
                            status === window.google.maps.GeocoderStatus.OK &&
                            result.length > 0
                        ) {
                            let {
                                place_id,
                                formatted_address,
                                geometry: { location }
                            } = result[0];
                            this.setState({
                                place_id,
                                formatted_address,
                                place_lat: location.lat(),
                                place_lng: location.lng()
                            });
                            innerScope.map.setCenter(location);
                            innerScope.marker.setPosition(location);
                            innerScope.infoWindow.setPosition(location);
                            innerScope.inputNode.value = formatted_address;
                            innerScope.infoWindow.setContent(`${formatted_address}`);
                            innerScope.infoWindow.open(innerScope.map);
                        }
                    }
                );
                /** when the marker is clicked */
                innerScope.marker.addListener("click", event => {
                    let location = innerScope.marker.getPosition();
                    innerScope.inputNode.value = this.state.formatted_address;
                    innerScope.infoWindow.setPosition(location);
                    innerScope.infoWindow.setContent(`${this.getInfoWindowContent()}`);
                    innerScope.infoWindow.open(innerScope.map);
                });
                /** after marker is left */
                innerScope.marker.addListener("dragend", event => {
                    let dragedPoint = innerScope.marker.getPosition();
                    innerScope.map.panTo(dragedPoint);

                    innerScope.geocoder.geocode({ latLng: dragedPoint },
                        (result, status) => {
                            if (
                                status === window.google.maps.GeocoderStatus.OK &&
                                result.length > 0
                            ) {
                                let {
                                    place_id,
                                    formatted_address,
                                    geometry: { location }
                                } = result[0];
                                this.setState({
                                    place_id,
                                    formatted_address,
                                    place_lat: location.lat(),
                                    place_lng: location.lng()
                                });
                                innerScope.map.setCenter(location);
                                innerScope.marker.setPosition(location);
                                innerScope.inputNode.value = formatted_address;
                                innerScope.infoWindow.setPosition(location);
                                innerScope.infoWindow.setContent(
                                    `${this.getInfoWindowContent()}`
                                );
                                innerScope.infoWindow.open(innerScope.map);
                            }
                        }
                    );
                });
                /** lisens for the place change */
                innerScope.autoComplete.addListener("place_changed", () => {
                    let place = innerScope.autoComplete.getPlace();
                    // return if the auto compleate is not selected from the drop down
                    if (!place.geometry) return;
                    let {
                        place_id,
                        formatted_address,
                        geometry: { location }
                    } = place;
                    this.setState({
                        place_id,
                        formatted_address,
                        place_lat: location.lat(),
                        place_lng: location.lng()
                    });
                    // bring the selected place in view on the innerScope.map
                    // innerScope.map.fitBounds(place.geometry.viewport);
                    innerScope.map.setCenter(location);
                    innerScope.marker.setPosition(location);
                    innerScope.infoWindow.setPosition(location);
                    innerScope.infoWindow.setContent(`${this.getInfoWindowContent()}`);
                    innerScope.infoWindow.open(innerScope.map);
                });

                innerScope.findMeOuter = position => {
                    let pos = new window.google.maps.LatLng(
                        position.coords.latitude,
                        position.coords.longitude
                    );
                    innerScope.geocoder.geocode({ latLng: pos }, (result, status) => {
                        if (
                            status === window.google.maps.GeocoderStatus.OK &&
                            result.length > 0
                        ) {
                            let {
                                place_id,
                                formatted_address,
                                geometry: { location }
                            } = result[0];
                            this.setState({
                                place_id,
                                formatted_address,
                                place_lat: location.lat(),
                                place_lng: location.lng()
                            });
                            innerScope.map.setCenter(location);
                            innerScope.marker.setPosition(location);
                            innerScope.infoWindow.setPosition(location);
                            innerScope.inputNode.value = formatted_address;
                            innerScope.infoWindow.setContent(`${formatted_address}`);
                            innerScope.infoWindow.open(innerScope.map);
                        }
                    });
                };
            };
            /**
             * a function used to give id of the table Row in db
             *
             * @param {event}
             *            event
             */
            this.onClick = event => {
                this.toggle();
                if (!event) return;
                event.preventDefault();
                event.target = Object.assign({},this.props,
                    {value: this.state,
                    stringValue: JSON.stringify(this.state)}
                );
                this.props.onChange && this.props.onChange(event);
            };
        }
        render() {
            return React.createElement(
                React.Fragment,
                null,
                React.createElement(
                    InputGroup, { type: "text", name: "name", id: this.popoverId },
                    React.createElement(Input, {
                        type: "text",
                        value: this.state.formatted_address,
                        readOnly: true,
                        disabled: !!this.props.disabled
                    }),
                    React.createElement(
                        InputGroupAddon, { addonType: "append" },
                        React.createElement(
                            Button, {
                                className: "mr-1 btn-success",
                                onClick: this.toggle,
                                color: "success",
                                disabled: !!this.props.disabled
                            },
                            React.createElement("i", { className: "icon-map" })
                        )
                    )
                ),
                React.createElement(
                    Popover, {
                        className: "gMapPopover",
                        placement: "bottom-end",
                        isOpen: this.state.mapOpen,
                        target: this.popoverId,
                        toggle: this.toggle
                    },
                    React.createElement(XMap, {
                        apiKey: _app.map_api,
                        onMapLoad: this.onMapLoad,
                        onClick: this.onClick
                    })
                )
            );
        }
    }
    /**
     * A component to render Masonry layout
     *
     * @param {Object}
     *            props.masonryRowStyle - style of the container
     * @param {Object}
     *            props.masonryStyle - style of the container
     * @param {Object}
     *            props.columnStyle - style of the column
     * @example ```jsx <XMasonry loadingComponent = {()=>{return '***********you can
     *          give loading component***********'}} breakPoints={[350, 500, 750]}
     *          loadNext={({columns,totalItems}) => { {columns,totalItems} - use
     *          this to construct url}} >{ this.state.photos.map((image, id) =>(
     *          <img key={id} src={image}/> ) ) } </XMasonry> ```
     */
class XMasonry extends React.Component {
        constructor(props) {
            super(props);
            this.state = {
                columns: 1,
                prevY: 0,
                loading: false
            };
            /**
             * a funntion used to calculate columns when resized
             */
            this.onResize = () => {
                const columns = this.getColumns(this.refs.Masonry.offsetWidth);
                if (columns !== this.state.columns) {
                    this.setState({ columns: columns });
                }
            };
            /**
             * a function used to calculate columns from this.props.breakPoints
             *
             * @param {Number}
             *            width - width of the masonry component
             */
            this.getColumns = width => {
                return (
                    this.props.breakPoints.reduceRight((p, c, i) => {
                        return c < width ? p : i;
                    }, this.props.breakPoints.length) + 1
                );
            };
            /**
             * a function used to calculate children according to column size
             */
            this.mapChildren = () => {
                let col = [];
                const numC = this.state.columns;
                for (let i = 0; i < numC; i++) {
                    col.push([]);
                }
                return this.props.children.reduce((p, c, i) => {
                    p[i % numC].push(c);
                    return p;
                }, col);
            };
            /**
             * a function used to call loadNext method to make lazyLoading from the rest
             *
             * @param {*}
             *            entities
             * @param {*}
             *            observer
             */
            this.handleObserver = (entities, observer) => {
                const y = entities[0].boundingClientRect.y;
                if (this.state.prevY > y) {
                    this.props.loadNext && this.setState({ loading: true });
                    this.props.loadNext &&
                        !this.props.loadNext({
                            columns: this.mapChildren().length,
                            totalItems: this.props.children.reduce((tot, ch) => tot + 1, 0)
                        }) &&
                        this.setState({ loading: false });
                }
                this.setState({ prevY: y });
            };
        }

        componentDidUpdate(prevProps, prevState, snapshot) {
            if (
                prevProps.children.length < this.props.children.length &&
                this.props.loadNext
            ) {
                this.setState({ loading: false });
            }
            if (prevProps.breakPoints.length !== this.props.breakPoints.length) {
                this.onResize();
            }
            return true;
        }
        componentDidMount() {
            // initial resize
            this.onResize();
            // add listener for window object
            window.addEventListener("resize", this.onResize, true);
            if (this.props.loadNext) {
                // Create an observer
                this.observer = new IntersectionObserver(
                    this.handleObserver.bind(this), // callback
                    {
                        root: null, // Page as root
                        rootMargin: "0px",
                        threshold: 0.01
                    }
                );
                // Observ the `loadingRef`
                this.observer.observe(this.refs.loadingRef);
            }
        }
        componentWillUnmount() {
            window.removeEventListener("resize", this.onResize, true);
        }
        render() {
            const masonryStyle = this.props;
            return _(
                Row, Object.assign({},{
                    className: 'xMasonryRoot overflowY-auto scrollY'}, 
                    this.props.root
                ),
                _(
                    "div", Object.assign({},{
                        className: "d-flex flex-row justify-content-center align-content-stretch flex-fill m-auto w-100",
                        style: masonryStyle,
                        ref: "Masonry"}
                        ,this.props.rootInner
                    ),
                    this.mapChildren().map((col, ci) => {
                        return _(
                            Col, { className: "pr-2 pl-2", style: this.props.columnStyle, key: ci },
                            col.map((child, i) => {
                                return _(
                                    Card, Object.assign({},{ key: i, className: "mt-2 mb-2"}, this.props.item ),
                                    child
                                );
                            })
                        );
                    })
                ),
                _(
                    "div", {
                        ref: "loadingRef",
                        style: {
                            height: "10%",
                            width: "100%",
                            margin: "0px",
                            display: this.props.loadNext ? "block" : "none"
                        }
                    },
                    _(
                        "span", { style: { display: this.state.loading ? "block" : "none" } },
                        this.props.loadingComponent ?
                        this.props.loadingComponent() :
                        "Loading..."
                    )
                )
            );
        }
    }
    /**
     * XAjaxQueryData - function is used to get data by giving guery id
     * @param {String} props.qui - query id that you want to get data from
     * @param {Function} props.middleMan
     * @param {Symbol} props.children
     * @example
     * React.createElement(XAjaxQueryData,{},data=>{ return React.createElement(AnyComponent,{data}......) }
     */
const XAjaxQueryData = ({
    onSuccess = console.log,
    qid = -1,
    params = {},
    url = null,
    children
}) => {
    const [data, setData] = React.useState([]);
    React.useEffect(() => {
        iwb.request({
            url: url || "ajaxQueryData?" + "_qid=" + qid,
            params: params || {},
            successCallback: ({ data }) => {
                let result = typeof onSuccess === "function" ? onSuccess(data) : data;
                setData(result);
            },
            errorCallback: cfg =>
                toastr.error("AjaxQueryData ERROR", "AjaxQueryData", { timeOut: 3000 })
        });
    }, []);
    return typeof children === "function" ? children(data) : children;
};
XAjaxQueryData.propTypes = {
    qid: PropTypes.oneOfType([PropTypes.string, PropTypes.number]).isRequired,
    children: PropTypes.element.isRequired,
    url: PropTypes.string,
    params: PropTypes.object,
    onSuccess: PropTypes.func
};
/**
 * A function to load script from the CDN or filesystem and apply css
 * @param {String} props.css - query id that you want to get data from
 * @param {Array/String} props.loadjs - used to define which script to download see exapmle below
 * @param {Array/String} props.loadcss - used to define which css script to download see exapmle below
 * @param {Symbol} props.loading - conponent to show loading indicator while feching scripts from CDN or static file
 * @param {Symbol} props.children
 * @example
 * _(XLazyScriptLoader,{loading:React.createElement(CustomLoadingComponent,{options}),css:`.customClassName{color:red}`,loadjs:['CDN','CDN2']||'CDN' }, childNode )
 */
const XLazyScriptLoader = ({ loadcss, loadjs, css, loading, children }) => {
    const [fetching, setFetching] = React.useState(true);
    const load = (() => {
        var _load = tag => {
            return src => {
                return new Promise((resolve, reject) => {
                    let element = document.createElement(tag);
                    let parent = "body";
                    let attr = "src";
                    element.onload = e => resolve(src);
                    element.onerror = e => reject(src);
                    switch (tag) {
                        case "script":
                            element.async = false;
                            break;
                        case "link":
                            element.type = "text/css";
                            element.rel = "stylesheet";
                            attr = "href";
                            parent = "head";
                            break;
                        default:
                    }
                    element[attr] = src;
                    window.document[parent].appendChild(element);
                });
            };
        };
        return { css: _load("link"), js: _load("script") };
    })();
    React.useEffect(() => {
        /** componentDidMount */
        let arrayProm = [];
        loadcss &&
            arrayProm.push(
                ...(loadcss.constructor === Array ?
                    loadcss.map(item => load.css(item)) :
                    [load.css(loadcss)])
            );
        loadjs &&
            arrayProm.push(
                ...(loadjs.constructor === Array ?
                    loadjs.map(item => load.js(item)) :
                    [load.js(loadjs)])
            );
        Promise.all(arrayProm)
            .then(() => {
                setFetching(false);
            })
            .catch(err => {
                console.error("XLazyScriptLoader", err);
            });
        iwb.addCssString(css);
    }, []);
    return fetching ? loading : children;
};
XLazyScriptLoader.defaultProps = {
    loading: "LOADING...."
};
XLazyScriptLoader.propTypes = {
    loading: PropTypes.oneOfType([PropTypes.func, PropTypes.string]),
    loadcss: PropTypes.oneOfType([PropTypes.array, PropTypes.string]),
    loadjs: PropTypes.oneOfType([PropTypes.array, PropTypes.string]),
    css: PropTypes.string
};
const XPreviewFile = ({ file }) => {
    let type = file ? file.type : null;
    let style = {
        fontSize: "12em"
    };
    if(type == "image/png" || type == "image/jpeg" || type == "image/jpg" || type == "image/gif" ){
    	return _("img", {
            src: URL.createObjectURL(file),
            className: "img-fluid rounded"
        });
    }
    if(type == "text/csv" || type == "text/plain"){
    	return _("div", { className: "m-auto text-center" },
        		_("i", {style,className: "fas fa-file-alt m-auto"}),
    	        _("br", null),
    	        file ? file.name : ""
    	    );
    }
    if(type == "application/vnd.openxmlformats-officedocument.spreadsheetml.sheet" 
    	|| type == "application/vnd.openxmlformats-officedocument.wordprocessingml.document"
    		|| type == "application/vnd.ms-excel"){
    	return _("div", { className: "m-auto text-center" },
    		_("i", {style,className: "fas fa-file-alt m-auto"}),
	        _("br", null),
	        file ? file.name : ""
	    );
    }
    if(type == "application/pdf"){
    	return _("div", { className: "m-auto text-center" },
        		_("i", {style,className: "fas fa-file-pdf m-auto"}),
    	        _("br", null),
    	        file ? file.name : ""
    	    );
    }
    return _(
            "div", { className: "m-auto text-center" },
            file ?
            _("i", { className: "far fa-file", style }) :
            _("i", { className: "fas fa-upload", style }),
            _("br", null),
            getLocMsg(file ? "undefined_type" : "choose_file_or_drag_it_here")
        );
};
class XListFiles extends React.Component {
    constructor() {
            super();
            this.state = {
                files: []
            };
            this.getFileList = this.getFileList.bind(this);
            this.deleteItem = this.deleteItem.bind(this);
            this.downladLink = this.downladLink.bind(this);
        }
        /** run query to get data based on pk and id */
    getFileList() {
        iwb.request({
            url: "ajaxQueryData?_qid=61&xtable_id=" +
                this.props.cfg.crudTableId +
                "&xtable_pk=" +
                (this.props.cfg.tmpId ?
                    this.props.cfg.tmpId :
                    json2pk(this.props.cfg.pk)) +
                "&.r=" +
                Math.random(),
            successCallback: ({ data }) => {
                this.setState({
                    files: data
                });
            }
        });
    }
    deleteItem(fileItem) {
            return event => {
                event.preventDefault();
                event.stopPropagation();
                /** deleteRequest */
                iwb.request({
                    url: "ajaxPostForm?a=3&_fid=1383&tfile_attachment_id=" +
                        fileItem.file_attachment_id,
                    successCallback: res => {
                        this.setState({
                            files: this.state.files.filter(
                                file => file.file_attachment_id != fileItem.file_attachment_id
                            )
                        });
                    }
                });
            };
        }
        /** test */
    downladLink(fileItem) {
        let url =
            "dl/" +
            fileItem.original_file_name +
            "?_fai=" +
            fileItem.file_attachment_id +
            "&.r=" +
            Math.random();
        return event => {
            event.preventDefault();
            event.stopPropagation();
            const link = document.createElement("a");
            link.href = url;link.target="_blank";
            document.body.appendChild(link);
            link.click();
            document.body.removeChild(link);
        };
    }
    componentDidMount() {
        this.getFileList();
    }
    render() {
        return _(
            ListGroup, {},
            this.state.files.map(fileItem =>
                _(
                    ListGroupItem,
                    null,
                    _(
                        "a", { onClick: this.downladLink(fileItem), href: "#" },
                        fileItem.original_file_name
                    ),
                    _("i", {
                        key: fileItem.file_attachment_id,
                        onClick: this.deleteItem(fileItem),
                        style: { cursor: "pointer" },
                        className: "icon-trash float-right text-danger"
                    })
                )
            )
        );
    }
}
class XSingleUploadComponent extends React.Component {
    constructor() {
            super();
            this.state = {
                canUpload: false,
                dragOver: false,
                file: null
            };
            this.xListFilesRef = React.createRef();
            this.onDrop = this.onDrop.bind(this);
            this.dragenter = this.dragenter.bind(this);
            this.dragleave = this.dragleave.bind(this);
            this.dragover = this.dragover.bind(this);
            this.onDeleteFile = this.onDeleteFile.bind(this);
            this.onclick = this.onclick.bind(this);
            this.onchange = this.onchange.bind(this);
            this.uploadFile = this.uploadFile.bind(this);
        }
        /** function to click input ref click */
    onclick(event) {
            event.preventDefault();
            event.stopPropagation();
            this.inpuRef.click();
        }
        /** used to disable opening file on new tab */
    dragover(event) {
            event.preventDefault();
            event.stopPropagation();
        }
        /** used with css */
    dragleave(event) {
            event.preventDefault();
            event.stopPropagation();
            this.setState({
                dragOver: false
            });
        }
        /** when the file over drag area */
    dragenter(event) {
            event.preventDefault();
            event.stopPropagation();
            this.setState({
                dragOver: true
            });
        }
        /** when the file dproped over drop area */
    onDrop(event) {
            event.preventDefault();
            event.stopPropagation();
            this.setState({
                    canUpload: true,
                    dragOver: false,
                    file: event.dataTransfer.files[0]
                },
                () => {
                    this.uploadFile();
                }
            );
        }
        /** when the file dproped over drop area */
    onchange(event) {
            event.preventDefault();
            event.stopPropagation();
            this.setState({
                    canUpload: true,
                    dragOver: false,
                    file: event.target.files[0]
                },
                () => {
                    this.uploadFile();
                }
            );
        }
        /** remove file from form state */
    onDeleteFile(event) {
            event.preventDefault();
            event.stopPropagation();
            /** will reset to null currently uploaded file */
            this.setState({
                canUpload: false,
                file: null
            });
        }
        /** uploader function */
    uploadFile() {
        // event.preventDefault();
        // event.stopPropagation();
        if (!this.state.file) {
            return;
        }
        let formData = new FormData();
        formData.append(
            "table_pk",
            this.props.cfg.tmpId ? this.props.cfg.tmpId : json2pk(this.props.cfg.pk)
        );
        formData.append("table_id", this.props.cfg.crudTableId);
        formData.append("file", this.state.file);
        formData.append("profilePictureFlag", this.props.profilePictureFlag || 0);
        fetch("upload.form", {
                method: "POST",
                body: formData,
                cache: "no-cache",
                credentials: "same-origin",
                mode: "cors",
                redirect: "follow",
                referrer: "no-referrer"
            })
            .then(
                response =>
                response.status === 200 || response.status === 0 ?
                response.json() :
                Promise.reject(new Error(response.text() || response.statusText))
            )
            .then(
                result => {
                    if (result.success) {
                        toastr.success(
                            getLocMsg("file_sucessfully_uploaded"),
                            getLocMsg("success"), {
                                timeOut: 3000
                            }
                        );
                        this.xListFilesRef.current.getFileList();
                        this.setState({
                            file: null,
                            canUpload: false
                        });
                    } else {
                        if (result.error) {
                            toastr.error(result.error, result.errorType);
                        }
                        return;
                    }
                },
                error => {
                    toastr.error(error, getLocMsg("error"));
                }
            );
    }
    render() {
        let defaultStyle = {
            height: "100%",
            width: "100%",
            position: "absolute",
            top: "0",
            left: "0"
        };
        return _(
            React.Fragment, {},
            _(
                Button, {
                    id: this.props.cfg.id,
                    type: "button",
                    className: "float-right btn-round-shadow mr-1",
                    color: "light"
                },
                _("i", {
                    className: "icon-paper-clip"
                }),
                this.props.cfg.fileAttachCount ?
                " " + this.props.cfg.fileAttachCount :
                ""
            ),
            _(
                Reactstrap.UncontrolledPopover, {
                    trigger: "legacy",
                    placement: "auto",
                    target: this.props.cfg.id
                },
                _(
                    PopoverHeader,
                    null,
                    this.state.file ?
                    getLocMsg(this.state.file.name) :
                    getLocMsg("File Upload"),
                    _("input", {
                        className: "d-none",
                        type: "file",
                        onChange: this.onchange,
                        ref: input => (this.inpuRef = input)
                    }),
                    this.props.extraButtons
                ),
                _(
                    PopoverBody,
                    null,
                    _(
                        "div", {
                            style: {
                                height: "200px",
                                width: "200px",
                                position: "relative",
                                border: this.state.dragOver ?
                                    "3px dashed #20a8d8" :
                                    "3px dashed #a4b7c1"
                            }
                        },
                        _("div", {
                            style: Object.assign({},defaultStyle,
                                {zIndex: "10",
                                background: "gray",
                                cursor: "pointer",
                                opacity: this.state.canUpload ? "0" : "0.5"
                            }),
                            className: "rounded",
                            onDrop: this.onDrop,
                            onDragEnter: this.dragenter,
                            onDragLeave: this.dragleave,
                            onDragOver: this.dragover,
                            onClick: this.onclick
                        }),
                        _(
                            "div", {
                                style: Object.assign({},defaultStyle,
                                    {display: "flex"
                                })
                            },
                            _(XPreviewFile, {
                                file: this.state.file
                            })
                        )
                    ),
                    _("div", {
                        className: "clearfix"
                    }),
                    _(XListFiles, { cfg: this.props.cfg, ref: this.xListFilesRef })
                )
            )
        );
    }
}

class XFormConversion extends React.Component {
    constructor(props) {
        super(props);
        var s = {};
        if (iwb.formConversions[props.id]) s = iwb.conversionForms[props.id];
        else props.conversionForms.map(i => (s[i.xid] = i.checked));
        this.state = s;
        this.onClick = this.onClick.bind(this);
        iwb.formConversions[this.props.id] = s;
    }
    onClick(event) {
        var xid = event.target.getAttribute("xid");
        if (xid) {
            var s = this.state;
            s[xid] = !s[xid];
            this.setState(s);
            iwb.formConversions[this.props.id] = s;
        }
    }
    render() {
        return _(
            "div", {},
            _("div", { className: "form-cnv" }, "Conversions"),
            _(
                "div", {},
                this.props.conversionForms.map(i => {
                    var pi = {
                        type: "checkbox",
                        className: "switch-input",
                        xid: i.xid,
                        checked: this.state[i.xid] || false,
                        onChange: this.onClick
                    };
                    return _(
                        FormGroup, { style: { marginBottom: "0.3rem" } },
                        _(
                            Label, {
                                className: "switch switch-xs switch-3d switch-warning",
                                style: { "margin-top": 3 }
                            },
                            _(Input, pi),
                            _("span", { className: "switch-label" }),
                            _("span", { className: "switch-handle" })
                        ),
                        _(
                            Label, { style: { marginLeft: "1rem" } },
                            _("b", null, [" [E-MAIL]", " [SMS]"][i.smsMailTip - 1]),
                            " " + i.text,
                            i.previewFlag && _("i", null, " (preview)")
                        )
                    );
                })
            )
        );
    }
}

class XFormSmsMailTemplate extends React.Component {
    constructor(props) {
        super(props);
        var s = {};
        if (iwb.formSmsMailTemplates[props.id])
            s = iwb.formSmsMailTemplates[props.id];
        else props.smsMailTemplates.map(i => (s[i.xid] = i.checked));
        this.state = s;
        this.onClick = this.onClick.bind(this);
        iwb.formSmsMailTemplates[props.id] = s;
    }
    onClick(event) {
        var xid = event.target.getAttribute("xid");
        if (xid) {
            var s = this.state;
            s[xid] = !s[xid];
            this.setState(s);
            iwb.formSmsMailTemplates[this.props.id] = s;
        }
    }
    render() {
        return _(
            "div", {},
            _("div", { className: "form-cnv" }, "SMS/Email Notifications"),
            _(
                "div", {},
                this.props.smsMailTemplates.map(i => {
                    var pi = {
                        type: "checkbox",
                        className: "switch-input",
                        xid: i.xid,
                        checked: this.state[i.xid] || false,
                        onChange: this.onClick
                    };
                    return _(
                        FormGroup, { style: { marginBottom: "0.3rem" } },
                        _(
                            Label, {
                                className: "switch switch-xs switch-3d switch-warning",
                                style: { "margin-top": 3 }
                            },
                            _(Input, pi),
                            _("span", { className: "switch-label" }),
                            _("span", { className: "switch-handle" })
                        ),
                        _(
                            Label, { style: { marginLeft: "1rem" } },
                            _("b", null, [" [E-MAIL]", " [SMS]"][i.smsMailTip - 1]),
                            " " + i.text,
                            i.previewFlag && _("i", null, " (preview)")
                        )
                    );
                })
            )
        );
    }
}

/**
 * @description used to render tab and show active tab on the full XPage
 * @param {Object}
 *            props.body - it renders bodyForm class wich came from the backend
 * @param {Object}
 *            props.cfg - config of the form [edit or intest, id of the form]
 * @param {Object}
 *            props.parentCt - [xpage]-a function to open and close tab from the
 *            form
 * @param {Object}
 *            props.callAttributes - extra props to XTabForm
 * @param {Object}
 *            props.callAttributes.openEditable - open form in edit mode
 */
class XTabForm extends React.PureComponent {
        constructor(props) {
            if (iwb.debug) console.log("XTabForm.constructor", props);
            super(props);
            this.state = {
                viewMode: (_app.form_auto_edit && 1*_app.form_auto_edit) || (this.props.callAttributes && this.props.callAttributes.openEditable) ?
                    false :
                    this.props.cfg.a == 1
            };
            /**
             * a function to make editable and non editable
             */
            this.toggleViewMode = () =>
                this.setState({ viewMode: !this.state.viewMode });
            /**
             * a function to send form data
             *
             * @param {Event}
             *            event
             */
            this.onSubmit = event => {
                event && event.preventDefault && event.preventDefault();
                var selfie = this;
                if (this.form) {
                    iwb.loadingActive(() => {
                        this.form.submit({
                            callback: (json, cfg) => {
                                var url = "showForm";
                                if (json.outs) {
                                    url += "?a=1&_fid=" + json.formId;
                                    for (var key in json.outs)
                                        url += "&t" + key + "=" + json.outs[key];
                                } else {
                                    url += cfg.url.substring("ajaxPostForm".length);
                                }
                                // console.log(selfie.props);
                                selfie.props.callAttributes.callback &&
                                    selfie.props.callAttributes.callback(json, cfg);
                                toastr.success(
                                    "Click! To see saved item <a href=# onClick=\"return iwb.openForm('" +
                                    url +
                                    "')\"></a>",
                                    "Saved Successfully", {
                                        timeOut: 3000
                                    }
                                );
                                if (json.msgs)
                                    for (var ri = 0; ri < json.msgs.length; ri++) {
                                        toastr.info(json.msgs[ri], "", {
                                            timeOut: 5000
                                        });
                                    }
                                var { parentCt } = selfie.props;
                                if (parentCt) {
                                    !!selfie.props.callAttributes.modal ?
                                        iwb.closeModal() :
                                        iwb.closeTab();
                                    iwb.onGlobalSearch2 && iwb.onGlobalSearch2("");
                                }
                                if (json.conversionPreviews)
                                    for (var ri = 0; ri < json.conversionPreviews.length; ri++) {
                                        var cnv = json.conversionPreviews[ri];
                                        iwb.openTab(
                                            "2-" + cnv._fid + "-" + cnv._cnvId,
                                            "showForm?a=2&_fid=" +
                                            cnv._fid +
                                            "&_cnvId=" +
                                            cnv._cnvId +
                                            "&_cnvTblPk" +
                                            cnv._cnvTblPk, {}, {
                                                modal: false
                                            }
                                        );
                                    }
                                if (json.smsMailPreviews)
                                    for (var ri = 0; ri < json.smsMailPreviews.length; ri++) {
                                        var fsm = json.smsMailPreviews[ri]; // [{"tbId":2783,"tbPk":43,"fsmId":424,"fsmTip":1}]
                                        iwb.openTab(
                                            "2-" + fsm.fsmId + "-" + fsm.tbPk,
                                            "showForm?a=2&_fid=4903&_cnvId=" +
                                            cnv._cnvId +
                                            "&_cnvTblPk" +
                                            cnv._cnvTblPk, {}, {
                                                modal: false
                                            }
                                        );
                                    }
                            }
                        });
                    });
                } else alert("this.form not set");
                return false;
            };
            this.onContSubmit = event => {
                event && event.preventDefault && event.preventDefault();
                var selfie = this;
                if (this.form) {
                    this.form.submit({
                        callback: (json, cfg) => {
                            var url = "showForm";
                            if (json.outs) {
                                url += "?a=1&_fid=" + json.formId;
                                for (var key in json.outs)
                                    url += "&t" + key + "=" + json.outs[key];
                            } else {
                                url += cfg.url.substring("ajaxPostForm".length);
                            }
                            //console.log(selfie.props);
                            selfie.props.callAttributes.callback &&
                                selfie.props.callAttributes.callback(json, cfg);
                            toastr.success(
                                "Click! To see saved item <a href=# onClick=\"return iwb.openForm('" +
                                url +
                                "')\"></a>",
                                "Saved Successfully", { timeOut: 3000 }
                            );
                        }
                    });
                } else alert("this.form not set");
                return false;
            };
            /**
             * a function to delete current editing record
             *
             * @param {event}
             *            event
             */
            this.deleteRecord = event => {
                event && event.preventDefault && event.preventDefault();
                let { formId, pk } = this.props.cfg;
                let pkz = "";
                for (let key in pk) {
                    pkz += "&" + key + "=" + pk[key];
                }
                let url = "ajaxPostForm?a=3&_fid=" + formId + pkz;
                yesNoDialog({
                    text: getLocMsg("are_you_sure"),
                    callback: success =>
                        success &&
                        iwb.request({
                            url,
                            successCallback: () => this.props.parentCt.closeTab(event, success)
                        })
                });
            };
            this.approvalAction = (action, xformId) => {
                return event => {
                    event && event.preventDefault && event.preventDefault();
                    let { formId, pk } = this.props.cfg;
                    let pkz = "";
                    for (let key in pk) {
                        pkz += "&" + key + "=" + pk[key];
                    }
                    let url = "";
                    switch (action) {
                        case 901: // start approval
                            url =
                                "ajaxApproveRecord?_aa=901&_arid=" +
                                this.props.cfg.approval.approvalRecordId;
                            yesNoDialog({
                                text: getLocMsg("are_you_sure"), //"Are you Sure to Start Approval?",
                                callback: success =>
                                    success &&
                                    iwb.request({
                                        url,
                                        params: { _adsc: "start approval" },
                                        successCallback: () =>
                                            this.props.parentCt.closeTab(event, success)
                                    })
                            });
                            break;
                        default:
                            url =
                                "ajaxApproveRecord?_aa=" +
                                action +
                                "&_arid=" +
                                this.props.cfg.approval.approvalRecordId;
                            var strAction = ["", "Approve", "Return", "Reject"][action];
                            // var p = prompt("Please enter comment",
                            // ["","Approve","Return","Reject"][action]);
                            // if(p){
                            if (xformId) {
                                //        	  console.log('this.props.cfg',this.props.cfg)
                                var formUrl = "showForm?a=2&_fid=" + xformId + pkz;
                                iwb.openTab(
                                    "1199",
                                    formUrl, {}, {
                                        modal: true,
                                        modalSize: this.props.cfg.subModalSize || false,
                                        callback: result => {
                                            if (!result.errorType && !result.error && !result.errors)
                                                iwb.request({
                                                    url,
                                                    params: {
                                                        _adsc: strAction,
                                                        _avno: this.props.cfg.approval.versionNo
                                                    },
                                                    successCallback: () =>
                                                        this.props.parentCt.closeTab(event, true)
                                                });
                                            else iwb.requestErrorHandler(result);
                                        }
                                    }
                                );
                            } else
                            	if(action!=3)
                                yesNoDialog({
                                    text: getLocMsg("are_you_sure"), //"Are you Sure to "+strAction+"?",
                                    callback: success =>
                                        success &&
                                        iwb.request({
                                            url,
                                            params: {
                                                _adsc: strAction,
                                                _avno: this.props.cfg.approval.versionNo
                                            },
                                            successCallback: () =>
                                                this.props.parentCt.closeTab(event, true)
                                        })
                                });else {
                                	var rejectStr = prompt(getLocMsg("reject_reason"));
                                	if(rejectStr)iwb.request({
                                        url,
                                        params: {
                                            _adsc: rejectStr,
                                            _avno: this.props.cfg.approval.versionNo
                                        },
                                        successCallback: () =>
                                            this.props.parentCt.closeTab(event, true)
                                    })
                                }
                            // }
                            break;
                    }
                };
            };
            this.extrabuttonClicked = props => event => {
                event.preventDefault();
                event.stopPropagation();
                let formData = this.form.getValues();
                let gridData =
                    this.props.callAttributes && this.props.callAttributes.rowData;
                props.click(event, { gridData, formData }, this);
            };
        }

        render() {
            let {
                props: {
                    body,
                    parentCt: { closeTab },
                    cfg: { deletable, name, extraButtons }
                },
                state: { viewMode },
                // methods
                onSubmit,
                onContSubmit,
                deleteRecord,
                approvalAction,
                toggleViewMode
            } = this;
            let formBody = _(body, { parentCt: this, viewMode });
            if (!formBody) return null;
            return _(
                Form, { onSubmit: event => event.preventDefault(), className:viewMode?"xview-mode":"" },
                _(
                    CardBlock, { className: "card-body" },
                    _(
                        "h3", {
                            className: "form-header mr-1"
                        } /* _("i",{className:"icon-star form-icon"})," ", */ ,
                        name, !this.props.cfg.viewMode &&
                        viewMode &&
                        _(
                            Button, {
                                color: "light",
                                className: "btn-form-edit mx-1",
                                onClick: toggleViewMode
                            },
                            _("i", { className: "icon-pencil mr-1" }),
                            getLocMsg("edit")
                        ),
                        viewMode &&
                        _(
                            Button, {
                                color: "light",
                                className: "btn-form-edit mx-1",
                                onClick: iwb.closeTab
                            },
                            getLocMsg("close")
                        ),
                        viewMode &&
                        deletable &&
                        _(
                            Button, {
                                color: "danger",
                                className: "btn-form-edit mx-1",
                                onClick: deleteRecord
                            },
                            _("i", { className: "icon-trash mr-1" }),
                            getLocMsg("delete")
                        ),
                        extraButtons &&
                        extraButtons.map(extraProps => {
                            switch (extraProps.type) {
                                case "button":
                                    let cls = extraProps.icon;
                                    return _(
                                        extraProps.$ || Button, {
                                            key: extraProps.text,
                                            className: "btn-form-edit mx-1 btn-success ",
                                            onClick: this.extrabuttonClicked(extraProps)
                                        },
                                        cls && _("span", {
                                            className: "mr-1 " + cls
                                        }),
                                        getLocMsg(extraProps.text || "")
                                    );
                                case "text":
                                    return _(
                                        FormGroup, {},
                                        _(
                                            Label, { className: "inputLabel", htmlFor: extraProps.name },
                                            extraProps.label
                                        ),
                                        _(extraProps.$ || Input, extraProps)
                                    );
                                default:
                                    return _(extraProps.$ || "span", extraProps);
                            }
                        }),
                        false &&
                        _(
                            Button, {
                                className: "float-right btn-round-shadow hover-shake",
                                color: "danger"
                            },
                            _("i", { className: "icon-options" })
                        ),
                        " ",
                        this.props.cfg.commentFlag &&
                        _(
                            Button, {
                                className: "float-right btn-round-shadow mr-1",
                                color: "light"
                            },
                            _("i", { className: "icon-bubbles" })
                        ),
                        " ",
                        this.props.cfg.fileAttachFlag &&
                        _(XSingleUploadComponent, {
                            cfg: this.props.cfg
                        }),
                        _("br"),
                        this.props.cfg.approval &&
                        _(
                            "div", {
                                style: {
                                    fontSize: "1rem",
                                    marginTop: 5,
                                    color: "#03A9F4",
                                    zoom: 1.2
                                }
                            },
                            _("i", { className: "icon-shuffle" }),
                            // " step ",
                            _(
                                "span",
                                null,
                                " " +
                                this.props.cfg.approval.dsc +
                                (this.props.cfg.approval.stepDsc ?
                                    " > " + this.props.cfg.approval.stepDsc :
                                    "")
                            ),
                            "    ",
                            this.props.cfg.approval.status > 997 &&
                            this.props.cfg.approval.status < 1000 &&
                            _(
                                "span", {
                                    className: "badge badge-pill badge-" +
                                        iwb.approvalColorMap[this.props.cfg.approval.status]
                                },
                                this.props.cfg.approval.status == 998 ?
                                "approved" :
                                "rejected"
                            )
                        ),
                        this.props.cfg.approval &&
                        this.props.cfg.approval.wait4start &&
                        _(
                            Button, {
                                color: "success",
                                className: "btn-form-edit",
                                onClick: approvalAction(901)
                            },
                            _("i", { className: "icon-support" }),
                            " ",
                            iwb.btnStartApprovalLabel || getLocMsg("start_approval")
                        ),
                        this.props.cfg.approval &&
                        this.props.cfg.approval.versionNo &&
                        _(
                            Button, {
                                color: "primary",
                                className: "btn-form-edit",
                                onClick: approvalAction(
                                        1,
                                        this.props.cfg.approval.approveFormId || false
                                    ) // approve
                            },
                            this.props.cfg.approval.btnApproveLabel || getLocMsg("approve")
                        ),
                        " ",
                        this.props.cfg.approval &&
                        this.props.cfg.approval.returnFlag &&
                        _(
                            Button, {
                                color: "warning",
                                className: "btn-form-edit",
                                onClick: approvalAction(
                                        2,
                                        this.props.cfg.approval.returnFormId || false
                                    ) // return
                            },
                            this.props.cfg.approval.btnReturnLabel || getLocMsg("return")
                        ),
                        " ",
                        this.props.cfg.approval &&
                        this.props.cfg.approval.versionNo &&
                        this.props.cfg.approval.reject !== false &&
                        _(
                            Button, {
                                color: "danger",
                                className: "btn-form-edit",
                                onClick: approvalAction(
                                        3,
                                        this.props.cfg.approval.rejectFormId || false
                                    ) // reject
                            },
                            iwb.btnApprovalRejectLabel || getLocMsg("reject")
                        ),
                        " ",
                        iwb.btnApprovalLogs4Form !== false &&
                        this.props.cfg.approval &&
                        this.props.cfg.approval.approvalRecordId &&
                        _(
                            Button, {
                                color: "light",
                                className: "btn-form-edit",
                                onClick: iwb.approvalLogs(
                                        this.props.cfg.approval.approvalRecordId
                                    ) // reject
                            },
                            iwb.btnApprovalLogsLabel || getLocMsg("workflow_logs")
                        )
                    ),
                    this.props.cfg.msgs &&
                    this.props.cfg.msgs.length &&
                    _(
                        "div", { style: { color: "#838383" } },
                        this.props.cfg.msgs.map(qq =>
                            _(
                                "div",
                                null,
                                _("i", { className: "icon-flag" }),
                                // " step ",
                                _("span", null, " " + qq)
                            )
                        )
                    ),
                    _("hr"),
                    formBody, !viewMode &&
                    this.props.cfg.conversionForms &&
                    _(XFormConversion, {
                        id: this.props.cfg.id,
                        conversionForms: this.props.cfg.conversionForms
                    }), !viewMode &&
                    this.props.cfg.smsMailTemplates &&
                    _(XFormSmsMailTemplate, {
                        id: this.props.cfg.id,
                        smsMailTemplates: this.props.cfg.smsMailTemplates
                    })
                ), !viewMode &&
                _(
                    CardFooter, { style: { padding: "1.1rem 1.25rem" } },
                    _(
                        Button, {
                            type: "submit",
                            color: "primary",
                            className: "btn-form mr-1",
                            onClick: onSubmit
                        },
                        " ",
                        getLocMsg("save"),
                        " "
                    ),
                    " ",
                    this.props.cfg.contFlag &&
                    _(
                        Button, {
                            type: "submit",
                            color: "secondary",
                            className: "btn-form mr-1",
                            onClick: onContSubmit
                        },
                        " ",
                        getLocMsg("save_and_coninue"),
                        " "
                    ),
                    " ",
                    _(
                        Button, {
                            color: "light",
                            style: { border: ".5px solid #e6e6e6" },
                            className: "btn-form",
                            onClick: this.props.callAttributes.modal ?
                                iwb.closeModal :
                                iwb.closeTab
                        },
                        getLocMsg("cancel")
                    )
                )
            );
        }
    }
    /**
     * @description Used for PopUp a Modal it is singletone and you can use
     * @example iwb.showModal(cfg); iwb.closeModal
     * @param {Object}
     *            props -props of the Xmodal
     */
class XModal extends React.Component {
        constructor(props) {
            super(props);
            this.state = { modal: false };
            /**
             * @description Used to construct Modal (popup)
             * @example iwb.showModal(cfg); iwb.closeModal
             * @param {Object}
             *            cfg - Moadal Configuration
             * @param {String}
             *            cfg.title - Moadal title
             * @param {String}
             *            cfg.color - Moadal Color 'primary'
             * @param {String}
             *            cfg.size - Moadal Size 'lg' 'md' 'sm'
             * @param {Symbol}
             *            cfg.body - Moadal body React.Component
             * @param {Object}
             *            cfg.style - Moadal style
             * @param {Object}
             *            cfg.footer - Moadal Configuration
             * @param {Object}
             *            cfg.modalBodyProps - Moadal Body Props to pass to the body of
             *            the modal
             */
            this.open = cfg => {
                this.setState({
                    modal: true,
                    body: cfg.body,
                    style: cfg.style,
                    footer: cfg.footer,
                    size: cfg.size || "lg",
                    title: cfg.title || "Form",
                    color: cfg.color || "primary",
                    modalBodyProps: cfg.modalBodyProps || {},
                    props: cfg.props || {}
                });
                return false;
            };
            /**
             * @description Used to close the Modal (actually hide)
             */
            this.close = () => this.setState({ modal: false });
            this.toggle = () => this.setState({ modal: !this.state.modal });
            iwb.showModal = this.open;
            iwb.closeModal = this.close;
        }

        render() {
            const {
                modal,
                footer,
                size,
                style,
                color,
                title,
                modalBodyProps,
                body,
                props
            } = this.state;
            return (
                modal &&
                _(
                    Modal, Object.assign({},{
                        keyboard: true,
                        onExit: () => {
                            iwb.loadingDeactive();
                        },
                        backdrop: footer !== false ? "static" : true,
                        toggle: this.toggle,
                        isOpen: modal,
                        className: "modal-" + size + " primary",
                        style},
                        props
                    ),
                    _(
                        ModalHeader, {
                            toggle: this.toggle,
                            className: "bg-" + color
                        },
                        title
                    ),
                    _(ModalBody, modalBodyProps, body),

                    // !footer && _(ModalFooter, null,
                    // _(Button, {
                    // className:'btn-form',
                    // color: 'teal',
                    // onClick: this.toggle
                    // },"KAYDET"),
                    // ' ',
                    // _(Button, {
                    // className:'btn-form',
                    // color: "light",
                    // style:{border: ".5px solid #e6e6e6"},
                    // onClick: this.toggle
                    // }, "VAZGEÇ")
                    // ),

                    footer && _(React.Fragment, null, footer)
                )
            );
        }
    }
    /**
     * @description this component used to login after session is timeout
     */
class XLoginDialog extends React.Component {
        constructor(props) {
            super(props);
            this.state = { modal: false, msg: false };
            /**
             * @description Used to open Modal we made it GLOBAL
             * @example iwb.showLoginDialog()
             */
            this.open = () => this.setState({ modal: true });
            iwb.showLoginDialog = this.open;
            /**
             * Used To Login User
             */
            this.login = () => {
                var self = this;
                var passWord = document.getElementById("id-password").value;
                if (!passWord) {
                    self.setState({ msg: "Önce Şifre Giriniz" });
                    return false;
                }
                iwb.request({
                    url: "ajaxAuthenticateUser?userRoleId=" +
                        _scd.userRoleId +
                        "&locale=" +
                        _scd.locale +
                        (_scd.projectId ? "&projectId=" + _scd.projectId : ""),
                    params: {
                        customizationId: _scd.customizationId,
                        userName: _scd.userName,
                        passWord: passWord,
                        locale: _scd.locale
                    },
                    callback: response => {
                        if (response.success) {
                            if (!response.waitFor) {
                                if (response.session) {
                                    _scd = response.session;
                                }
                                self.setState({ modal: false, msg: false });
                            } else {
                                self.setState({ msg: "TODO! " + response.waitFor });
                            }
                            return false;
                        } else {
                            self.setState({ msg: response.errorMsg });
                        }
                    },
                    errorCallback: j => {
                        this.setState({ msg: "Olmadi" });
                    }
                });
            };
        }

        render() {
            return _(
                Modal, {
                    centered: true,
                    keyboard: false,
                    backdrop: "static",
                    toggle: this.toggle,
                    isOpen: this.state.modal,
                    className: "modal-sm primary"
                },
                _(
                    ModalBody,
                    null,
                    _("h1", null, "Login"),
                    _(
                        "p", {
                            className: "text-muted",
                            style: { color: this.state.msg ? "red !important" : "" }
                        },
                        this.state.msg || "Session Timeout"
                    ),
                    _(
                        InputGroup, { className: "mb-3" },
                        _(
                            "div", { className: "input-group-prepend" },
                            _(
                                "span", { className: "input-group-text" },
                                _("i", { className: "icon-user" })
                            )
                        ),
                        _(Input, {
                            type: "text",
                            readOnly: true,
                            value: _scd.userName,
                            placeholder: "Username"
                        })
                    ),
                    _(
                        InputGroup, { className: "mb-4" },
                        _(
                            "div", { className: "input-group-prepend" },
                            _(
                                "span", { className: "input-group-text" },
                                _("i", { className: "icon-lock" })
                            )
                        ),
                        _(Input, {
                            type: "password",
                            id: "id-password",
                            placeholder: "Password"
                        })
                    )
                ),
                _(
                    ModalFooter,
                    null,
                    _(Button, { color: "primary", onClick: this.login }, "Login"),
                    " ",
                    _(
                        Button, {
                            color: "secondary",
                            onClick: () => (document.location = "login.htm?.r=" + Math.random())
                        },
                        "Exit"
                    )
                )
            );
        }
    }
    /**
     * @description used to open dropDown make edit and delete from main and detail
     *              grid when the Grid is not in edit mode
     * @param {
     *            Object } props - gets data of right click and crud
     * @param {
     *            Array } props.menuButtons - return array of Objects conf { text,
     *            handler, cls }
     * @param {
     *            Boolean } props.crudFlags.edit -ACL Edit Option
     * @param {
     *            Boolean } props.crudFlags.insert -ACL insert Option
     * @param {
     *            Boolean } props.crudFlags.remove -ACL Delete Option
     * @param {
     *            Array } props.rowData - data of the clicked Row
     */
class XGridRowAction extends React.PureComponent {
    constructor(props) {
        super(props);
        if (iwb.debug) console.log("XGridRowAction", props);
        this.state = { isOpen: false };
        this.toggle = event => {
            event.preventDefault();
            event.stopPropagation();
            this.setState({ isOpen: !this.state.isOpen });
        };
    }
    render() {
        const {
            state: { isOpen },
            props: {
                tag,
                rowData,
                parentCt,
                className,
                menuButtons,
                onEditClick,
                onDeleteClick,
                formSmsMailList,
                crudFlags: { edit, remove }
            },
            toggle
        } = this;
        return _(
            Dropdown, { isOpen, toggle, className, tag },
            _(DropdownToggle, {
                tag: "i",
                className: "icon-options-vertical column-action"
            }),
            isOpen &&
            _(
                DropdownMenu, { className: isOpen ? "show" : "", style: { fontSize: "small" } },
                edit &&
                _(
                    DropdownItem, {
                        key: "123",
                        onClick: event => {
                            onEditClick({ event, rowData, openEditable: true });
                        }
                    },
                    _("span", { className: "mr-2 icon-pencil" }),
                    getLocMsg("edit")
                ),
                remove &&
                _(
                    DropdownItem, {
                        key: "1223",
                        onClick: event => {
                            onDeleteClick({ event, rowData });
                        }
                    },
                    _("span", {
                        className: "mr-2 icon-minus text-danger"
                    }),
                    getLocMsg("delete")
                ),
                menuButtons &&
                menuButtons.map(
                    ({
                        text = "ButtonTextWillBeHere",
                        handler = (event, rowData, parentCt) => {
                            console.group();
                            console.warn("No Render Method! event, rowData, parentCt ");
                            console.table([
                                { event: event, rowData: rowData, parentCt: parentCt }
                            ]);
                        },
                        cls = ""
                    }) => {
                        //                cls = cls.split("|");
                        return _(
                            DropdownItem, {
                                key: text,
                                onClick: event =>
                                    handler.call(this.state, event, rowData, parentCt),
                                //                    className: cls[1]
                            },
                            _("span", { className: "mr-2 icon-" + cls }),
                            text
                        );
                    }
                ),
                /** mail buttonst */
                formSmsMailList &&
                formSmsMailList.map(
                    (rest) => {
                    	var text = rest.text || "ButtonTextWillBeHere";
                        var handler = rest.handler || ((event, rowData, parentCt, rest) => {
                                // iwb.openForm
                                iwb.openTab(
                                    "1-" + Math.random(),
                                    "showForm?a=2&&_fid=5748", {}, {
                                        modal: true
                                    }
                                );
                            });
                        var xid = rest.xid; 
                        return _(
                            DropdownItem, {
                                key: xid,
                                onClick: event =>
                                    handler.call(this.state, event, rowData, parentCt, rest)
                            },
                            _("span", { className: "mr-2 fas fa-at text-warning" }),
                            text
                        );
                    }
                )
                /** mail buttonst */
            )
        );
    }
}
XGridRowAction.propTypes = {
    tag: PropTypes.string,
    rowData: PropTypes.object,
    parentCt: PropTypes.obj,
    menuButtons: PropTypes.arrayOf(
        PropTypes.shape({
            text: PropTypes.string,
            handler: PropTypes.func,
            cls: PropTypes.string
        })
    ),
    formSmsMailList: PropTypes.arrayOf(PropTypes.object),
    onEditClick: PropTypes.func,
    onDeleteClick: PropTypes.func,
    crudFlags: PropTypes.shape({
        edit: PropTypes.bool,
        remove: PropTypes.bool
    })
};
XGridRowAction.defaultProps = {
    tag: "span"
};

class XGridRowAction2 extends React.PureComponent {
    constructor(props) {
        super(props);
        if (iwb.debug) console.log("XGridRowAction2", props);
        this.state = { isOpen: false };
        this.toggle = event => {
            event.preventDefault();
            event.stopPropagation();
            this.setState({ isOpen: !this.state.isOpen });
        };
    }
    render() {
        const {
            state: { isOpen },
            props: {
                tag,
                rowData,
                parentCt,
                className,
                menuButtons,
                onEditClick,
                onDeleteClick,
                formSmsMailList,
                crudFlags: { edit, remove }
            },
            toggle
        } = this;
        return _(
            'div', {  },
             edit &&
                _(
                    Button, {title:getLocMsg("edit"),
                        key: "123",color:'warning',
                        onClick: event => {
                            onEditClick({ event, rowData, openEditable: true });
                        }
                    },
                    _("span", { className: "icon-pencil" })// text-warning
                ),
                remove &&
                _(
                		Button, {title:getLocMsg("delete"),
                        key: "1223", color:'danger',
                        onClick: event => {
                            onDeleteClick({ event, rowData });
                        }
                    },
                    _("span", {
                        className: "icon-minus"//
                    })
                )/*,
                menuButtons &&
                menuButtons.map(
                    ({
                        text = "ButtonTextWillBeHere",
                        handler = (event, rowData, parentCt) => {
                            console.group();
                            console.warn("No Render Method! event, rowData, parentCt ");
                            console.table([
                                { event: event, rowData: rowData, parentCt: parentCt }
                            ]);
                        },
                        cls = ""
                    }) => {
                        //                cls = cls.split("|");
                        return _(
                            DropdownItem, {
                                key: text,
                                onClick: event =>
                                    handler.call(this.state, event, rowData, parentCt),
                                //                    className: cls[1]
                            },
                            _("span", { className: "mr-2 icon-" + cls }),
                            text
                        );
                    }
                ),
                formSmsMailList &&
                formSmsMailList.map(
                    ({
                        text = "ButtonTextWillBeHere",
                        handler = (event, rowData, parentCt, rest) => {
                            // iwb.openForm
                            iwb.openTab(
                                "1-" + Math.random(),
                                "showForm?a=2&&_fid=5748", {}, {
                                    modal: true
                                }
                            );
                        },
                        xid,
                        ...rest
                    }) => {
                        return _(
                            DropdownItem, {
                                key: xid,
                                onClick: event =>
                                    handler.call(this.state, event, rowData, parentCt, rest)
                            },
                            _("span", { className: "mr-2 fas fa-at text-warning" }),
                            text
                        );
                    }
                )
*/
        );
    }
}
XGridRowAction2.propTypes = {
    tag: PropTypes.string,
    rowData: PropTypes.object,
    parentCt: PropTypes.obj,
    menuButtons: PropTypes.arrayOf(
        PropTypes.shape({
            text: PropTypes.string,
            handler: PropTypes.func,
            cls: PropTypes.string
        })
    ),
    formSmsMailList: PropTypes.arrayOf(PropTypes.object),
    onEditClick: PropTypes.func,
    onDeleteClick: PropTypes.func,
    crudFlags: PropTypes.shape({
        edit: PropTypes.bool,
        remove: PropTypes.bool
    })
};
XGridRowAction2.defaultProps = {
    tag: "span"
};
/**
 * @deprecated todo: not used yet
 */
class XGridAction extends React.PureComponent {
        constructor(props) {
            super(props);
            this.toggle = () => this.setState({ isOpen: !this.state.isOpen });
            this.state = { isOpen: false };
        }
        render() {
            const {
                state: { isOpen },
                props: { color },
                toggle
            } = this;
            return _(
                Dropdown, { isOpen, toggle },
                // ,_('i',{className:'icon-options-vertical column-action',
                // onClick:qqq.toggleGridAction})
                _(
                    DropdownToggle, {
                        tag: "div",
                        className: "timeline-badge hover-shake " + color,
                        onClick: () => alert("hehey")
                    },
                    _("i", { className: "fa fa-plus",  style: { fontSize: 19 } })
                ),
                // {tag:'i',className: "icon-grid", color||'danger'}
                isOpen &&
                _(
                    DropdownMenu, { className: isOpen ? "show" : "" },
                    // ,_('div',{style:{padding: "7px 13px",background: "gray", color:
                    // "darkorange", fontWeight: "500", fontSize:" 16px"}},'İşlemler')
                    _(
                        DropdownItem, { ur: "123", onClick: false },
                        _("i", {
                            className: "fa fa-plus",
                            style: {
                                marginRight: 5,
                                marginLeft: -2,
                                fontSize: 12,
                                color: "#777"
                            }
                        }),
                        getLocMsg("new_record")
                    ),
                    _("hr"),
                    _(
                        DropdownItem, { ur: "1223", onClick: false },
                        _("i", {
                            className: "icon-equalizer",
                            style: {
                                marginRight: 5,
                                marginLeft: -2,
                                fontSize: 12,
                                color: "#777"
                            }
                        }),
                        getLocMsg("reports_bi")
                    )
                    // ,_(DropdownItem,{ur:'1223',onClick:false},_('i',{className:'icon-drop',style:{marginRight:5,
                    // marginLeft:-2, fontSize:12,color:'#777'}}),'Diğer İşlemler')
                )
            );
        }
    }
    /**
     * @description it renders detail grid there is no search form
     * @param {Object}
     *            props - Input of the Grid Component
     * @param {Array}
     *            props.columns[] - Column conf List {name title width sort}
     * @param {Object}
     *            props.crudFlags - Grid Component {edit insert remove} options Used
     *            to render CRUD buttons and routes
     * @param {Number}
     *            props.crudFormId - crudFormId is used to make route to the form
     * @param {Number}
     *            props.crudTableId - crudTableId is id of sql table
     * @param {Number}
     *            props.defaultHeight -
     * @deprecated defaultHeight is a height of the Grid
     * @param {Number}
     *            props.defaultWidth -
     * @deprecated defaultWidth is width of the Grid
     * @param {Boolean}
     *            props.detailFlag - Am I detail grid?
     * @param {Boolean}
     *            props.editable - Used to Open Grid in EditingState
     *            Mode############
     * @param {Number}
     *            props.gridId - Id of the Detail grid
     * @param {String}
     *            props.keyField - Used to spesify primety Key name of the Grid
     * @param {String}
     *            props.name - Rendered name of the Grid Component
     * @param {Function}
     *            props.openTab - Used to open Form in new tab
     * @param {Function}
     *            props.pageSize - [0] by default
     * @param {Number}
     *            props.queryId - Query id of the grid
     * @param {Symbol}
     *            props._disableIntegratedGrouping - ['null'] Disable Grouping
     * @param {Symbol}
     *            props._disableIntegratedSorting - ['null'] Disable sorting
     * @param {Symbol}
     *            props._disableSearchPanel - ['null'] Disable search panel
     * @param {Symbol}
     *            props.multiselect - ['null'] Enambe multiselect option
     * @param {Symbol}
     *            props.showDetail - ['null'] detail grid used in nested detail grid
     */

class XGrid extends GridCommon {
    constructor(props) {
        super(props);
        if (iwb.debug) console.log("XGrid", props);
        if(props.setCmp)props.setCmp(this);
        var columns = [], columnFilters = [];
        var columnExtensions = [];
        const canIOpenActions =
            (props.crudFlags && (props.crudFlags.edit || props.crudFlags.remove)) ||
            props.menuButtons;
        

        var colTemp = props.columns;
        colTemp &&
            colTemp.map(colLocal => {
            	if(!colLocal.hidden){
	                var title;
	                switch (colLocal.name) {
	                    case "pkpkpk_faf":
	                        title = _("i", { className: "icon-paper-clip" });
	                        break;
	                    case "pkpkpk_ms":
	                        title = _("i", { className: "icon-envelope" });
	                        break;
	                    case "pkpkpk_cf":
	                        title = _("i", { className: "icon-bubble" });
	                        break;
	                    case "pkpkpk_apf":
	                        title = _("i", { className: "icon-picture" });
	                        break;
	                    case "pkpkpk_vcsf":
	                        title = _("i", { className: "icon-social-github" });
	                        break;
	                }
	
	                columns.push({
	                    name: colLocal.name,
	                    title: title || colLocal.title,
	                    getCellValue: colLocal.formatter || undefined
	                });
	                columnExtensions.push({
	                    width: +colLocal.width,
	                    columnName: colLocal.name,
	                    align: colLocal.align || "left",
	                    sortingEnabled: !!colLocal.sort
	                });
	                if(colLocal.filter)
	                	columnFilters.push({columnName:colLocal.name, filteringEnabled:!0})
            	}
            });
        
        if(!columnFilters.length)columnFilters = null;
        if (canIOpenActions) {
            columns.push({
                name: "_qw_",
                title: " ",
                getCellValue: rowData => {
                    var { onEditClick, onDeleteClick } = this;
                    return _(XGridRowAction2, Object.assign({},
                        { rowData },
                        { onEditClick, onDeleteClick },
                        { crudFlags: props.crudFlags },
                        { menuButtons: props.menuButtons },
                        { formSmsMailList: props.formSmsMailList },
                        { parentCt: this }
                    ));
                }
            });
            columnExtensions.push({
                columnName: "_qw_",
                width: 80,
                align: "center",
                sortingEnabled: false
            });
        }
        this.state = {
            columns, columnFilters, filtersEnabled: !!columnFilters && !props._initHiddenFilters,
            sorting: [],
            totalCount: 0,
            currentPage: 0,
            loading: false,
            columnExtensions,
            gridActionOpen: false,
            rows: props.rows || [],
            order: columns.map(({ name }) => name),
            pageSize: props.pageSize || iwb.detailPageSize,
            columnWidths: columnExtensions.map(({ columnName, width }) => {
                return { columnName, width };
            }),
            pageSizes: props.pageSize > 1 ?
                [] : //parseInt(props.pageSize / 2), props.pageSize, 3 * props.pageSize
                [5, 10, 25, 100]
        };
        
        if(props._initHiddenFilters)this.toggleFilters = () =>{
        	this.setState({filtersEnabled:!this.state.filtersEnabled});
        }
        /**
         * @overloading
         * @description used to make request and fill the grid
         * @param {boolean}
         *            force - to fill with up to date data
         */
        this.loadData = force => {
            if (this.props.rows || this.state.loading) return;
            const queryString = this.queryString();
            if (!force && queryString === this.lastQuery) {
                return;
            }
            this.setState({ /*rows: [], */loading: true });
            var params = this.props.searchForm && iwb.getFormValues(document.getElementById(this.props.searchForm.id));
            if(this.props.beforeLoad && this.props.beforeLoad(this, params)===false)return;
            iwb.request({
                url: queryString,
                self: this,
                params: params,
                successCallback: (result, cfg) => {
                	if(cfg.self.props.onLoad && cfg.self.props.onLoad(result, cfg)===false)return;
                    cfg.self.setState({
                        rows: result.data,
                        totalCount: result.total_count,
                        loading: false
                    });
                },
                errorCallback: (error, cfg) => {
                    cfg.self.setState({
                        rows: [],
                        totalCount: 0,
                        loading: false
                    });
                }
            });
            this.lastQuery = queryString;
        };
        if(props.registerLoad)props.registerLoad(this.loadData);
    }
    componentDidMount() {
        if (!this.dontRefresh) this.loadData();
        this.dontRefresh = false;
    }
    componentDidUpdate() {
        this.loadData();
        this.dontRefresh = false;
    }
    componentWillUnmount() {
        iwb.grids[this.props.id] = Object.assign({}, this.state);
    }
    render() {
        const {
            state: {
                rows, loading,
                order,
                columns, columnFilters, filtersEnabled,
                sorting,
                pageSize,
                pageSizes,
                totalCount,
                currentPage,
                columnWidths,
                columnExtensions
            },
            props: {
                keyField,
                showDetail,
                multiselect, extraButtons,
                groupColumn,
                _disableSearchPanel,
                _disableIntegratedSorting,
                _disableIntegratedGrouping,
                titleComponent, filterColumns
            },
            // methods
            rowComponent,
            onOrderChange,
            onSortingChange,
            onPageSizeChange,
            onCurrentPageChange,
            onColumnWidthsChange
        } = this;

        if (!this.props._showAlways && (!rows || !rows.length)) return null;
        return _(
            _dxgrb.Grid, { style:{opacity:loading?.5:1}, rows, columns, getRowId: row => row[keyField] },
            /** filtering */
            filtersEnabled && columnFilters && _(_dxrg.FilteringState, {columnFilteringEnabled:false, columnExtensions:columnFilters}),
            /** sorting */
            !_disableIntegratedSorting &&
            _(
                _dxrg.SortingState, !pageSize ? null : { sorting, onSortingChange, columnExtensions }
            ),
            /** state multiselect */
            multiselect && _(_dxrg.SelectionState, null),
            /** state search */
            !pageSize && _(_dxrg.SearchState, null),
            /** Client filtering */
            ((filtersEnabled && columnFilters) || (!_disableSearchPanel &&
            !pageSize &&
            rows.length > 1)) &&
            _(_dxrg.IntegratedFiltering, null),
            /** state grouping */
            (groupColumn || (!_disableIntegratedGrouping &&
            !pageSize &&
            rows.length > 1)) &&
            _(_dxrg.GroupingState, groupColumn ? {defaultGrouping:[{ columnName: groupColumn }]}:{}),
            /** Enable UI grouping */

            (groupColumn || (!_disableIntegratedGrouping &&
            !pageSize &&
            rows.length > 1)) &&
            _(_dxrg.IntegratedGrouping, null),
            /** state sorting */
            !_disableIntegratedSorting &&
            !pageSize &&
            rows.length > 1 &&
            _(_dxrg.IntegratedSorting, null),
            /** state detail grid */
            showDetail && _(_dxrg.RowDetailState, null),
            /** state paging */

            rows.length > iwb.detailPageSize && pageSize > 1 ?
            _(
                _dxrg.PagingState,
                pageSize > 1 ?
                { currentPage, pageSize, onCurrentPageChange, onPageSizeChange } :
                {}
            ) :
            null,
            /** UI paging */

            pageSize > 1 &&
            rows.length > iwb.detailPageSize &&
            _(_dxrg.CustomPaging, { totalCount }),
            /** multiselect */
            multiselect && _(_dxrg.IntegratedSelection, null),
            /** Enable Drag and Drop */
            _(_dxgrb.DragDropProvider, null),
            /** UI table */
            _(this.props._virtual ? _dxgrb.VirtualTable : _dxgrb.Table, {
                columnExtensions,
                rowComponent,
                messages: { noData: getLocMsg("no_data") }
            }),
            /** UI multiselect */
            multiselect && _(_dxgrb.TableSelection, { showSelectAll: multiselect }),
            /** UI ordering of the table */
            _(_dxgrb.TableColumnReordering, { order, onOrderChange }),
            /** UI column table resizer */
            _(_dxgrb.TableColumnResizing, { columnWidths, onColumnWidthsChange }),
            _(_dxgrb.TableHeaderRow, {
                showSortingControls: true,
                titleComponent: titleComponent || _dxgrb.TableHeaderRow.Title
            }),
            filtersEnabled && columnFilters && _(_dxgrb.TableFilterRow, {showFilterSelector:!0}),
            /** UI detail Grid */
            showDetail ?
            _(_dxgrb.TableRowDetail, { contentComponent: showDetail }) :
            null,
            /** Paging panel */
            rows.length > iwb.detailPageSize && pageSize>1 &&
            _(_dxgrb.PagingPanel, { pageSizes: pageSizes || iwb.detailPageSize }),
            /** UI row Grouping */
            !_disableIntegratedGrouping &&
            !pageSize &&
            _(_dxgrb.TableGroupRow, null), (groupColumn || (!pageSize && !_disableIntegratedGrouping &&
//            !_disableIntegratedSorting ||
//            !_disableSearchPanel ||
            rows.length > 0)) && _(_dxgrb.Toolbar, null), 
            /*!_disableSearchPanel &&
            !pageSize &&
            rows.length > 1 &&
            _(_dxgrb.SearchPanel, {
                messages: { searchPlaceholder: getLocMsg("search_placeholder") },
                changeSearchValue: ax => {
                    if (iwb.debug) console.log("onValueChange", ax);
                }
            }), */// TODO
            (groupColumn || (!_disableIntegratedGrouping &&
            !multiselect && !pageSize &&
            rows.length > 1)) &&
            _(_dxgrb.GroupingPanel, {
                showSortingControls: true,
                messages: { groupByColumn: getLocMsg("group_by_column") }
            })
        );
    }
}
/**
 * @description A functional component to glue button inside grid with its props
 * @param {Object}
 *            props - { id, onExecute }
 * @param {Number}
 *            props.id - index of the ComponentProps array
 * @param {Function}
 *            props.onExecute - a callback function to be executed when button
 *            is clicked
 */
const Command = ({ id, onExecute }) => {
    var ComponentProps = iwb.commandComponentProps[id];
    return (!!Object.keys(ComponentProps || {}).length &&
        _(
            Button, {
                color: "link",
                style: { padding: "11px" },
                onClick: onExecute,
                title: ComponentProps.hint
            },
            _(
                "span", { className: ComponentProps.color || "" },
                ComponentProps.icon &&
                _("i", {
                    className: "oi oi-" + ComponentProps.icon,
                    style: { marginRight: ComponentProps.text ? 5 : 0 }
                }),
                ComponentProps.text
            )
        )
    );
};
/**
 * @description can be used to overload grid functionality component for making
 *              GRIDROW Edit + Multiselect
 */
const SelectableStubCell = () =>
    _(
        Plugin, {},
        _(
            Template, {
                name: "tableCell",
                predicate: ({ tableRow, tableColumn }) => {
                    if (
                        tableRow.key !== "heading" &&
                        tableColumn.key === "select" &&
                        tableRow.type === "edit"
                    ) {
                        return true;
                    }
                }
            },
            ({ tableRow }) =>
            _(TemplateConnector, {}, ({ selection }, { toggleSelection }) =>
                _(_dxgrb.TableSelection.Cell, {
                    row: tableRow.row,
                    selected: selection.indexOf(tableRow.rowId) !== -1,
                    onToggle: () => toggleSelection({ rowIds: [tableRow.rowId] })
                })
            )
        )
    );
/**
 * @description used for sf grid in popup Modal
 * @param {Object}
 *            props - Input of the Grid Component
 * @param {Function}
 *            props.callback - used to send back selected data
 * @param {Array}
 *            props.columns[] - Column conf List {name title width sort}
 * @param {Boolean}
 *            props.crudFlags.edit - Grid Component {edit} options Used to
 *            render CRUD buttons and routes
 * @param {Number}
 *            props.defaultHeight -
 * @deprecated defaultHeight is a height of the Grid
 * @param {Number}
 *            props.defaultWidth -
 * @deprecated defaultWidth is width of the Grid
 * @param {Boolean}
 *            props.editable - Used to Open Grid in EditingState
 *            Mode############
 * @param {Number}
 *            props.gridId - Id of the grid grid
 * @param {String}
 *            props.gridReport -@deprecated usage
 * @param {String}
 *            props.keyField - Used to spesify primety Key name of the Grid
 * @param {String}
 *            props.name - Rendered name of the Grid Component
 * @param {Symbol}
 *            props.multiselect - ['null'] Enable multiselect option
 * @param {Function}
 *            props.pageSize - [10] by default
 * @param {Number}
 *            props.queryId - Query id of the grid
 * @param {Symbol}
 *            props.searchForm - Search form is generated from ServerSide and
 *            extens from XForm Component
 * @param {Object}
 *            props.selectRow - [{mode:"checkbox",clickToSelect: true}]Used to
 *            Edit and make Selectable
 * @param {Symbol}
 *            props._disableIntegratedGrouping - ['null'] Disable Grouping
 * @param {Symbol}
 *            props._disableIntegratedSorting - ['null'] Disable sorting
 * @param {Symbol}
 *            props._disableSearchPanel - ['null'] Disable search panel
 */
class XEditGridSF extends GridCommon {
    constructor(props) {
        if (iwb.debug) console.log("XEditGridSF.constructor", props);
        super(props);
        var oldGridState = iwb.grids[props.id];
        if (iwb.debug) console.log("oldGridState", oldGridState);
        if (oldGridState) {
            this.editors = {};
            this.dontRefresh = true;
            this.state = oldGridState;
            var colTemp = props.columns;
            colTemp &&
                colTemp.map(colLocal => {
                    if (colLocal.editor) {
                        this.editors[colLocal.name] = colLocal.editor;
                    }
                });
        } else {
            var columns = [];
            this.editors = {};
            var columnExtensions = [];
            var colTemp = props.columns;
            colTemp &&
                colTemp.map(colLocal => {
                    switch (colLocal.name) {
                        case "pkpkpk_faf":
                        case "pkpkpk_ms":
                        case "pkpkpk_cf":
                        case "pkpkpk_apf":
                        case "pkpkpk_vcsf":
                            break;
                        default:
                            columns.push({
                                name: colLocal.name,
                                title: colLocal.title,
                                getCellValue: colLocal.formatter || undefined
                            });
                            var editor = colLocal.editor || false;
                            if (editor) {
                                editor.autoComplete = "off";
                                if (!editor.style) editor.style = {};
                                editor.style.width = "100%";
                                switch (+editor._control) {
                                    case 6:
                                    case 8:
                                    case 58:
                                    case 7:
                                    case 15:
                                    case 59:
                                    case 9:
                                    case 10: // combos
                                        break;
                                    default:
                                        editor.style.textAlign = colLocal.align || "left";
                                }
                                this.editors[colLocal.name] = editor;
                            }
                            columnExtensions.push({
                                width: +colLocal.width,
                                editingEnabled: !!editor,
                                columnName: colLocal.name,
                                align: colLocal.align || "left",
                                sortingEnabled: !!colLocal.sort
                            });
                    }
                });
            this.state = {
                columns,
                rows: [],
                pkInsert: 0,
                sorting: [],
                selection: [],
                addedRows: [],
                totalCount: 0,
                deletedRows: [],
                rowChanges: {},
                loading: false,
                currentPage: 0,
                columnExtensions,
                deletingRows: [],
                editingRowIds: [],
                order: columns.map(({ name }) => name),
                pageSize: props.pageSize || iwb.detailPageSize,
                viewMode: !props.editable && (props.viewMode || true),
                columnWidths: columnExtensions.map(({ columnName, width }) => {
                    return { columnName, width };
                }),
                pageSizes: props.pageSize > 1 ?
                    [] ://parseInt(props.pageSize / 2), props.pageSize, 3 * props.pageSize
                    [5, 10, 25, 100]
            };
        }
        /**
         * used to get values of the grid
         */
        this.getValues = () => {
            let {
                rows,
                addedRows,
                deletedRows,
                editingRowIds,
                selection
            } = this.state;
            let addedRowsTemp = addedRows;
            rows = rows.slice();
            selection.forEach(rowId => {
                if (rowId > 0) {
                    const index = rows.findIndex(
                        row => row[this.props.keyField] === rowId
                    );
                    if (index > -1) {
                        addedRowsTemp = [Object.assign({},rows[index] )];
                    }
                }
            });
            var searchFormData =
                this.props.searchForm &&
                iwb.getFormValues(document.getElementById("s-" + this.props.id));
            // xsample_id to sample_id converter could be written as helper function
            searchFormData &&
                Object.keys(searchFormData).forEach((key, index) => {
                    if (key.charAt(0) === "x") {
                        searchFormData[key.slice(1)] = searchFormData[key];
                        delete searchFormData[key];
                    }
                });
            return {
                searchFormData,
                inserted: addedRowsTemp,
                deleted: deletedRows,
                _state: this.state,
                _this: this
            };
        };
        if (props.parentCt && props.parentCt.egrids)
            props.parentCt.egrids[props.gridId] = this;
        if (this.props.searchForm) {
            this.searchForm = _(
                Nav, { style: {} },
                _(
                    "div", { className: "hr-text" },
                    _("h6", null, getLocMsg("search_criteria"))
                ),
                _(
                    "div", { style: { zoom: ".9" }, className: "searchFormFields" },
                    _(this.props.searchForm, { parentCt: this }),
                    _(
                        "div", { className: "form-group", style: { paddingTop: 10 } },
                        _(
                            Button, {
                                color: "danger",
                                style: { width: "100%", borderRadius: 2 },
                                onClick: () => this.loadData(true)
                            },
                            getLocMsg("search")
                        )
                    )
                )
            );
        }
        /**
         * @param {Boolean}
         *            force
         */
        this.loadData = force => {
            if (this.state.loading) return;
            const queryString = this.props._url;
            const t_props = this.props;
            this.setState({ loading: true, rows: [] });
            iwb.request({
                url: queryString,
                self: this,
                params: this.props.searchForm &&
                    iwb.getFormValues(document.getElementById("s-" + this.props.id)),
                successCallback: (result, cfg) => {
                	if(cfg.self.props.onLoad && cfg.self.props.onLoad(result, cfg)===false)return;
                    var state = {
                        loading: false,
                        rows: result.data,
                        totalCount: result.total_count
                    };
                    state.editingRowIds = state.rows.map(row => row[t_props.keyField]);
                    cfg.self.setState(state);
                },
                errorCallback: (error, cfg) => {
                    cfg.self.setState({
                        rows: [],
                        totalCount: 0,
                        loading: false
                    });
                }
            });
            this.lastQuery = queryString;
        };
        this.EditCell = xprops => {
            var editor = this.editors[xprops.column.name];
            if (!editor) return _(_dxgrb.TableEditRow.Cell, xprops);

            editor = Object.assign({}, editor);
            if (!xprops.row._new) xprops.row._new = {}; // Object.assign({},xprops.row);
            if (!xprops.row._new.hasOwnProperty(xprops.column.name))
                xprops.row._new[xprops.column.name] = xprops.row[xprops.column.name];
            var keyFieldValue =
                xprops.row._new && xprops.row._new[this.props.keyField] ?
                xprops.row._new[this.props.keyField] :
                xprops.row[this.props.keyField];
            delete editor.defaultValue;
            switch (1 * editor._control) {
                case 3:
                case 4: // number
                    editor.value = xprops.value || 0; // xprops.row._new[xprops.column.name];
                    editor.onValueChange = ({ value }) => {
                        xprops.row._new[xprops.column.name] = value;
                        xprops.onValueChange(value);
                        this.props.onValueChange &&
                            this.props.onValueChange({
                                inthis: this,
                                keyFieldValue: keyFieldValue,
                                inputName: xprops.column.name,
                                inputValue: value
                            });
                    };
                    break;
                case 6:
                case 8:
                case 58:
                case 7:
                case 15:
                case 59:
                case 9:
                case 10: // combos
                    editor.value = xprops.row._new[xprops.column.name] || 0 || ""; // TODO.
                    // ilk
                    // edit
                    // ettigini
                    // aliyor
                    editor.onChange = ({ id }) => {
                        xprops.row._new[xprops.column.name] = id;
                        xprops.onValueChange(id);
                        this.props.onValueChange &&
                            this.props.onValueChange({
                                inthis: this,
                                keyFieldValue,
                                inputName: xprops.column.name,
                                inputValue: id
                            });
                    };
                    break;
                case 5: // checkbox
                    editor.checked = +xprops.row._new[xprops.column.name];
                    editor.onChange = ({ target: { checked } }) => {
                        xprops.row._new[xprops.column.name] = checked;
                        xprops.onValueChange(checked);
                        this.props.onValueChange &&
                            this.props.onValueChange({
                                inthis: this,
                                keyFieldValue,
                                inputName: xprops.column.name,
                                inputValue: checked
                            });
                    };
                    break;
                default:
                    editor.value = xprops.value || ""; // xprops.row._new[xprops.column.name];
                    editor.onChange = ({ target: { value } }) => {
                        xprops.row._new[xprops.column.name] = value;
                        xprops.onValueChange(value);
                        this.props.onValueChange &&
                            this.props.onValueChange({
                                inthis: this,
                                keyFieldValue,
                                inputName: xprops.column.name,
                                inputValue: value
                            });
                    };
                    break;
            }
            var cmp = Input;
            if (editor.$) {
                cmp = editor.$;
                delete editor.$;
            }
            return _(
                "td", { style: { verticalAlign: "middle", padding: 1 } },
                _(cmp, editor)
            );
        };
    }
    componentDidMount() {
        if (!this.dontRefresh) this.loadData();
    }
    componentDidUpdate() {
        if (this.props.editable && this.props.viewMode != this.state.viewMode) {
            this.setState({ viewMode: this.props.viewMode });
        }
    }
    componentWillUnmount() {
        iwb.grids[this.props.id] = Object.assign({}, this.state);
    }
    render() {
        if (iwb.debug) console.log("XEditGrid:render");
        const {
            state: {
                rows,
                order,
                loading,
                sorting,
                columns,
                viewMode,
                pageSize,
                addedRows,
                selection,
                pageSizes,
                rowChanges,
                totalCount,
                currentPage,
                columnWidths,
                editingRowIds,
                columnExtensions
            },
            props: {
                keyField,
                selectRow,
                crudFlags,
                multiselect,
                _importClicked,
                titleComponent,
                _disableSearchPanel,
                _disableIntegratedSorting,
                _disableIntegratedGrouping
            },
            // methods
            onOrderChange,
            onCommitChanges,
            onPageSizeChange,
            onAddedRowsChange,
            onSelectionChange,
            onRowChangesChange,
            onCurrentPageChange,
            onColumnWidthsChange,
            onEditingRowIdsChange
        } = this;
        var g = _(
            _dxgrb.Grid, {
                rows,
                columns,
                getRowId: row => row[keyField]
            }, !_disableIntegratedSorting ? _(_dxrg.SortingState, null) : null,

            multiselect &&
            _(_dxrg.SelectionState, {
                selection,
                onSelectionChange
            }),
            _(_dxrg.SearchState, null),
            /** Client filtering //was used for panel search(@dependency) */
            !_disableSearchPanel && _(_dxrg.IntegratedFiltering, null),
            /** state grouping */
            !_disableIntegratedGrouping && _(_dxrg.GroupingState, null),
            /** Enable UI grouping */

            !_disableIntegratedGrouping && _(_dxrg.IntegratedGrouping, null),
            /** state sorting */
            !_disableIntegratedSorting && _(_dxrg.IntegratedSorting, null),
            /** state paging */

            rows.length > iwb.detailPageSize &&
            _(
                _dxrg.PagingState,
                pageSize > 1 ?
                { pageSize, currentPage, onCurrentPageChange, onPageSizeChange } :
                {}
            ),

            multiselect && _(_dxrg.IntegratedSelection, null), !viewMode &&
            _(_dxrg.EditingState, {
                addedRows,
                rowChanges,
                editingRowIds,
                onCommitChanges,
                columnExtensions,
                onAddedRowsChange,
                onRowChangesChange,
                onEditingRowIdsChange
            }),

            _(_dxgrb.DragDropProvider, null),
            _(_dxgrb.Table, {
                columnExtensions,
                messages: { noData: getLocMsg("no_data") }
            }),
            multiselect &&
            _(_dxgrb.TableSelection, {
                showSelectAll: true
            }),
            /** UI ordering of the table */
            _(_dxgrb.TableColumnReordering, { order, onOrderChange }),
            /** UI tablle resizing */
            _(_dxgrb.TableColumnResizing, { columnWidths, onColumnWidthsChange }),
            _(_dxgrb.TableHeaderRow, {
                showSortingControls: true,
                titleComponent: titleComponent || _dxgrb.TableHeaderRow.Title
            }),
            selectRow.mode === "checkbox" && _(SelectableStubCell),

            !viewMode &&
            _(_dxgrb.TableEditRow, {
                cellComponent: this.EditCell
            }),

            !multiselect &&
            !viewMode &&
            _(_dxgrb.TableEditColumn, {
                showAddCommand: crudFlags && crudFlags.insert,
                showEditCommand: crudFlags && crudFlags.edit,
                commandComponent: Command,
                showDeleteCommand: crudFlags && crudFlags.remove
            }),

            rows.length > iwb.detailPageSize ?
            _(_dxgrb.PagingPanel, { pageSizes: pageSizes || iwb.detailPageSize }) :
            null, !_disableIntegratedGrouping ? _(_dxgrb.TableGroupRow, null) : null, !_disableIntegratedGrouping ||
            !_disableIntegratedSorting ||
            !_disableSearchPanel ?
            _(_dxgrb.Toolbar, null) :
            null, !_disableSearchPanel ?
            _(_dxgrb.SearchPanel, {
                messages: { searchPlaceholder: getLocMsg("search_placeholder") }
            }) :
            null, !_disableIntegratedGrouping ?
            _(_dxgrb.GroupingPanel, {
                showSortingControls: true,
                messages: { groupByColumn: getLocMsg("group_by_column") }
            }) :
            null
        );

        var footer = _(
            ModalFooter, {},
            _(
                Button, {
                    className: "btn-form px-1 mx-1",
                    color: "teal",
                    onClick: () => {
                        this.onCommitChanges(this.state);
                        if (this.props.callback(this.getValues()) === true)
                            iwb.closeModal();
                    }
                },
                getLocMsg("save")
            ),
            _(
                Button, {
                    className: "btn-form px-1 mx-1",
                    color: "light",
                    style: { border: ".5px solid #e6e6e6" },
                    onClick: iwb.closeModal
                },
                getLocMsg("cancel")
            )
        );

        return _("div", { className: "tab-grid mb-4" }, [!!this.searchForm &&
            _(
                "nav", { id: "sf-" + this.props.id, key: "sf-" + this.props.id },
                this.searchForm
            ),
            _("main", { className: "inbox", key: "inbox" }, g, footer)
        ]);
    }
}
/**
 * @description {name, children, predicate, position} used to extend template of
 *              the grid!
 * @param {
 *            object } param0
 * @param {
 *            string } param0.name - to find tample name
 * @param {
 *            Symbol } param0.children - React.Component
 * @param {
 *            Function } param0.predicate - is a function to deside where to
 *            render
 * @param {
 *            String } param0.position - ['before','after','',null] used to
 *            render before, after or override
 * @example overloading template example located in XEditGrid render
 */

const extendGrid = ({ name, children, predicate, position }) => {
    return _(
        Plugin,
        null,
        _(
            Template, {
                name,
                predicate: rest => predicate(rest)
            },
            params => {
                return _(
                    React.Fragment,
                    null,
                    position === "before" && children,
                    position && _(TemplatePlaceholder, null),
                    position === "after" && children,
                    position !== true && children
                );
            }
        )
    );
};
/**
 * @description {text,callback} used for making popup dialog
 * @param {object}
 *            conf.text - body of the mesasge
 * @param {object}
 *            conf.title - title of the modal
 * @param {function}
 *            conf.callback - callback function
 * @return {boolean} - retur true or false to the call back
 * @example yesNoDialog({ text:"Are you Sure!", callback:(success)=>{ logic here
 *          }});
 */
yesNoDialog = (confg) => {
	var text = confg.text || getLocMsg("are_you_sure");
	var title = confg.title || getLocMsg("confirmation");
	var callback = confg.callback || (()=>alert("obj.callback is not a function"));
    iwb.showModal(Object.assign({},{
        body: text,
        size: "sm",
        title: title,
        color: "danger",
        footer: _(
            ModalFooter,
            null,
            _(
                Button, {
                    className: "btn-form",
                    color: "teal",
                    onClick: () => {
                        callback(true);
                        iwb.closeModal();
                    }
                },
                getLocMsg("ok")
            ),
            " ",
            _(
                Button, {
                    className: "btn-form",
                    color: "light",
                    style: { border: ".5px solid #e6e6e6" },
                    onClick: () => {
                        callback(false);
                        iwb.closeModal();
                    }
                },
                getLocMsg("cancel")
            )
        ),
        
    },confg));
};
/**
 * @description component for edit Detail Grid mostly used for form + grid mode
 */
class XEditGrid extends GridCommon {
    constructor(props) {
        if (iwb.debug) console.log("XEditGrid.constructor", props);
        super(props);
        // state
        var oldGridState = iwb.grids[props.id];
        if (iwb.debug) console.log("oldGridState", oldGridState);
        if (oldGridState) {
            this.dontRefresh = true;
            this.state = oldGridState;
            var c = props.columns;
            this.editors = {};
            colTemp &&
                colTemp.map(colLocal => {
                    if (colLocal.editor) {
                        this.editors[colLocal.name] = editor;
                    }
                });
        } else {
            var columns = [],
                columnExtensions = [];
            var colTemp = props.columns;
            this.editors = {};
            colTemp &&
                colTemp.map(colLocal => {
                    switch (colLocal.name) {
                        case "pkpkpk_faf":
                        case "pkpkpk_ms":
                        case "pkpkpk_cf":
                        case "pkpkpk_apf":
                        case "pkpkpk_vcsf":
                            break;
                        default:
                            columns.push({
                                name: colLocal.name,
                                title: colLocal.title,
                                getCellValue: colLocal.formatter || undefined
                            });
                            var editor = colLocal.editor || false;
                            if (editor) {
                                editor.autoComplete = "off";
                                if (!editor.style) editor.style = {};
                                editor.style.width = "100%";
                                editor.style.position = "inherit";
                                switch (+editor._control) {
                                    case 6:
                                    case 8:
                                    case 58:
                                    case 7:
                                    case 15:
                                    case 59:
                                    case 9:
                                    case 10: // combos
                                        break;
                                    default:
                                        editor.style.textAlign = colLocal.align || "left";
                                }
                                this.editors[colLocal.name] = editor;
                            }
                            columnExtensions.push({
                                width: +colLocal.width,
                                editingEnabled: !!editor,
                                columnName: colLocal.name,
                                align: colLocal.align || "left",
                                sortingEnabled: !!colLocal.sort
                            });
                    }
                });

            this.state = {
                columns,
                rows: [],
                pkInsert: 0,
                sorting: [],
                totalCount: 0,
                addedRows: [],
                rowChanges: {},
                currentPage: 0,
                deletedRows: [],
                loading: false,
                columnExtensions,
                deletingRows: [],
                editingRowIds: [],
                order: columns.map(({ name }) => name),
                pageSize: props.pageSize || iwb.detailPageSize,
                viewMode: !props.editable && (props.viewMode || true),
                columnWidths: columnExtensions.map(({ columnName, width }) => {
                    return { columnName, width };
                }),
                pageSizes: props.pageSize > 1 ?
                    [] ://parseInt(props.pageSize / 2), props.pageSize, 3 * props.pageSize
                    [5, 10, 25, 100]
            };
        }
        // methods
        /**
         * used to get values of the grid
         */

        this.getValues = () => {
            let { rows, addedRows, deletedRows, editingRowIds } = this.state;
            rows = rows.slice();
            var changedRows = [];
            editingRowIds.forEach(rowId => {
                if (rowId > 0) {
                    const index = rows.findIndex(
                        row => row[this.props.keyField] === rowId
                    );
                    if (index > -1) changedRows.push(Object.assign({}, rows[index]));
                }
            });
            return {
                inserted: addedRows,
                deleted: deletedRows,
                changed: changedRows
            };
        };
        /**
         * bind with parent Element
         */
        if (props.parentCt && props.parentCt.egrids)
            props.parentCt.egrids[props.gridId] = this;
        /**
         * used to make data request to fill the frid with related data
         *
         * @param {boolean}
         *            force
         */
        this.loadData = force => {
            if (this.state.loading) return;
            const queryString = this.props._url;
            const t_props = this.props;
            this.setState({ rows: [], loading: true });
            iwb.request({
                url: queryString,
                self: this,
                params: this.props.searchForm &&
                    iwb.getFormValues(document.getElementById("s-" + this.props.id)),
                successCallback: (result, cfg) => {
                	if(cfg.self.props.onLoad && cfg.self.props.onLoad(result, cfg)===false)return;
                    var state = {
                        loading: false,
                        rows: result.data,
                        totalCount: result.total_count
                    };
                    if (t_props.multiselect) {
                        state.editingRowIds = state.rows.map(row => row[t_props.keyField]);
                    }
                    cfg.self.setState(state);
                    t_props.afterLoadData && t_props.afterLoadData(cfg.self);
                },
                errorCallback: (error, cfg) => {
                    cfg.self.setState({
                        rows: [],
                        totalCount: 0,
                        loading: false
                    });
                }
            });
            this.lastQuery = queryString;
        };
        /**
         * used for import data from the popup with ne flag
         */
        this.BulkyImport = ({ searchFormData, inserted, deleted, _state }) => {
            const { rows, addedRows } = this.state;
            let tempRow = [];
            let max;
            // find max tab_order from grid
            if (
                (rows["0"] && rows["0"].tab_order) ||
                (addedRows["0"] && addedRows["0"].tab_order)
            ) {
                max =
                    Math.max(
                        ...rows.map(d => +d.tab_order),
                        ...addedRows.map(d => +d.tab_order)
                    ) + 10;
            } else {
                max = (rows.length + addedRows.length) * 10;
            }
            if (max === "-Infinity" || +max === 0) {
                max = 10;
            }
            // merge new imported data
            let pkInsert = this.state.pkInsert;
            inserted.forEach(data => {
                var merged = Object.assign({}, searchFormData, data );
                merged = Object.assign({}, merged, merged._new );
                merged.tab_order = max;
                merged.max = max;
                --pkInsert;
                merged[this.props.keyField] = pkInsert;
                tempRow.push(merged);
                max += 10;
            });
            // Adds data to the grit from the popup
            this.setState({ addedRows: [...addedRows, ...tempRow], pkInsert });
        };
        /**
         * to get all data from grid editing + noneEdited at current time
         */

        this.getAllData = () => {
            let tempRowData = [];
            this.state.rows.forEach(data => {
                tempRowData.push(Object.assign({}, data, data._new ));
            });
            return tempRowData;
        };
        /**
         * used for Cell Editing
         *
         * @param {Object}
         *            xprops
         */
        this.EditCell = xprops => {
            var editor = this.editors[xprops.column.name];
            if (
                this.props.isCellEditable &&
                this.props.isCellEditable(xprops.row, xprops.column.name) === false
            )
                return _(_dxgrb.TableEditRow.Cell, Object.assign({}, xprops,
                    { editingEnabled: false }
                ));
            if (!editor) return _(_dxgrb.TableEditRow.Cell, xprops);
            editor = Object.assign({}, editor);
            if (!xprops.row._new) xprops.row._new = {}; // Object.assign({},xprops.row);
            if (!xprops.row._new.hasOwnProperty(xprops.column.name))
                xprops.row._new[xprops.column.name] = xprops.row[xprops.column.name];

            var keyFieldValue =
                xprops.row._new && xprops.row._new[this.props.keyField] ?
                xprops.row._new[this.props.keyField] :
                xprops.row[this.props.keyField];

            switch (1 * editor._control) {
                case 3:
                case 4: // number
                    editor.value =
                        xprops.row && xprops.row._new && xprops.row._new[xprops.column.name] ?
                        xprops.row._new[xprops.column.name] :
                        xprops.value;
                    editor.onValueChange = ({ value }) => {
                        xprops.row._new[xprops.column.name] = value;
                        xprops.onValueChange(value);
                        this.props.onValueChange &&
                            this.props.onValueChange({
                                inthis: this,
                                keyFieldValue: keyFieldValue,
                                inputName: xprops.column.name,
                                inputValue: value
                            });
                    };
                    break;
                case 6:
                case 8:
                case 58:
                case 7:
                case 15:
                case 59:
                case 9:
                case 10: // combos
                    editor.value = xprops.row._new[xprops.column.name]; // TODO. ilk
                    // edit ettigini
                    // aliyor
                    editor.onChange = ({ id }) => {
                        xprops.row._new[xprops.column.name] = id;
                        xprops.onValueChange(id);
                        this.props.onValueChange &&
                            this.props.onValueChange({
                                inthis: this,
                                keyFieldValue,
                                inputName: xprops.column.name,
                                inputValue: id
                            });
                    };
                    break;
                case 5:
                    editor.checked = +xprops.row._new[xprops.column.name];
                    editor.onChange = ({ target: { checked } }) => {
                        xprops.row._new[xprops.column.name] = checked;
                        xprops.onValueChange(checked);
                        this.props.onValueChange &&
                            this.props.onValueChange({
                                inthis: this,
                                keyFieldValue,
                                inputName: xprops.column.name,
                                inputValue: checked
                            });
                    };
                    break;
                default:
                    editor.value =
                        xprops.row && xprops.row._new && xprops.row._new[xprops.column.name] ?
                        xprops.row._new[xprops.column.name] :
                        xprops.value;
                    editor.onChange = ({ target: { value } }) => {
                        xprops.row._new[xprops.column.name] = value;
                        xprops.onValueChange(value);
                        this.props.onValueChange &&
                            this.props.onValueChange({
                                inthis: this,
                                keyFieldValue,
                                inputName: xprops.column.name,
                                inputValue: value
                            });
                    };
                    break;
            }
            delete editor.defaultValue;
            delete editor.defaultChecked;
            var cmp = Input;
            if (editor.$) {
                cmp = editor.$;
                delete editor.$;
            }
            return _(
                "td", { style: { verticalAlign: "middle", padding: 1 } },
                _(cmp, editor)
            );
        };
    }
    componentDidMount() {
        if (!this.dontRefresh) this.loadData();
    }
    componentDidUpdate() {
        if (this.props.editable && this.props.viewMode != this.state.viewMode) {
            this.setState({ viewMode: this.props.viewMode });
        }
    }
    componentWillUnmount() {
        iwb.grids[this.props.id] = Object.assign({}, this.state);
    }
    render() {
        const {
            state: {
                rows,
                order,
                columns,
                loading,
                sorting,
                pageSize,
                viewMode,
                pageSizes,
                addedRows,
                rowChanges,
                totalCount,
                currentPage,
                columnWidths,
                editingRowIds,
                columnExtensions
            },
            props: {
                keyField,
                crudFlags,
                multiselect,
                _importClicked,
                titleComponent,
                _disableSearchPanel,
                _disableIntegratedSorting,
                _disableIntegratedGrouping
            },
            onOrderChange,
            onCommitChanges,
            onPageSizeChange,
            onAddedRowsChange,
            onRowChangesChange,
            onCurrentPageChange,
            onColumnWidthsChange,
            onEditingRowIdsChange
        } = this;
        return _(
            _dxgrb.Grid, {
                rows,
                columns,
                getRowId: row => row[keyField]
            }, !_disableIntegratedSorting ? _(_dxrg.SortingState, null) : null,
            multiselect && _(_dxrg.SelectionState, null),
            _(_dxrg.SearchState, null), !_disableSearchPanel ? _(_dxrg.IntegratedFiltering, null) : null, // was
            // used
            // for
            // panel
            // search(@dependency)
            !_disableIntegratedGrouping ? _(_dxrg.GroupingState, null) : null, !_disableIntegratedGrouping ? _(_dxrg.IntegratedGrouping, null) : null, !_disableIntegratedSorting ? _(_dxrg.IntegratedSorting, null) : null,
            rows.length > iwb.detailPageSize ?
            _(
                _dxrg.PagingState,
                pageSize > 1 ?
                {
                    pageSize,
                    currentPage,
                    onPageSizeChange,
                    onCurrentPageChange
                } :
                {}
            ) :
            null,
            multiselect && _(_dxrg.IntegratedSelection, null), !viewMode &&
            _(_dxrg.EditingState, {
                addedRows,
                rowChanges,
                editingRowIds,
                onCommitChanges,
                columnExtensions,
                onAddedRowsChange,
                onRowChangesChange,
                onEditingRowIdsChange
            }),
            _(_dxgrb.DragDropProvider, null),
            _(_dxgrb.Table, {
                columnExtensions,
                messages: { noData: getLocMsg("no_data") }
            }),
            multiselect &&
            _(_dxgrb.TableSelection, {
                showSelectAll: true
            }),
            /** UI ordering of the table */
            _(_dxgrb.TableColumnReordering, { order, onOrderChange }),
            /** UI tablle resizing */
            _(_dxgrb.TableColumnResizing, { columnWidths, onColumnWidthsChange }),
            _(_dxgrb.TableHeaderRow, {
                showSortingControls: !_disableIntegratedSorting,
                titleComponent: titleComponent || _dxgrb.TableHeaderRow.Title
            }), !viewMode &&
            _(_dxgrb.TableEditRow, {
                cellComponent: this.EditCell
            }), !multiselect &&
            !viewMode &&
            _(_dxgrb.TableEditColumn, {
                showAddCommand: crudFlags && crudFlags.insert,
                showEditCommand: crudFlags && crudFlags.edit,
                showDeleteCommand: crudFlags && crudFlags.remove,
                commandComponent: Command
            }),

            _importClicked &&
            _(
                extendGrid, {
                    name: "tableCell",
                    predicate: rest => {
                        if (
                            rest.tableRow.key === "heading" &&
                            rest.tableColumn.key === "editCommand" &&
                            rest.tableRow.type === "heading"
                        ) {
                            return true;
                        }
                    }
                },
                _(TemplateConnector, {}, (getters, actions) => {
                    return _(
                        _dxgrb.TableEditColumn.HeaderCell, {},
                        crudFlags &&
                        crudFlags.insert &&
                        _(Command, {
                            id: "add",
                            onExecute: () => actions.addRow()
                        }),
                        _importClicked &&
                        _(Command, {
                            id: "import",
                            onExecute: () => _importClicked()
                        })
                    );
                })
            ),

            rows.length > iwb.detailPageSize ?
            _(_dxgrb.PagingPanel, { pageSizes: pageSizes || iwb.detailPageSize }) :
            null, !_disableIntegratedGrouping ? _(_dxgrb.TableGroupRow, null) : null, !_disableIntegratedGrouping ||
            !_disableIntegratedSorting ||
            !_disableSearchPanel ?
            _(_dxgrb.Toolbar, null) :
            null, !_disableSearchPanel ?
            _(_dxgrb.SearchPanel, {
                messages: { searchPlaceholder: getLocMsg("search_placeholder") }
            }) :
            null, !_disableIntegratedGrouping ?
            _(_dxgrb.GroupingPanel, {
                showSortingControls: true,
                messages: { groupByColumn: getLocMsg("group_by_column") }
            }) :
            null
        );
    }
}
/**
 * a Component to render item of toolbar
 */
const XToolbarItem = props => {
    if (props.type === "button") {
        let { icon } = props;
        return _(
            Button, {
                id: "toolpin" + props.index,
                key: "key" + props.index,
                className: "tlb-button mx-1",
                color: "gray",
                onClick: e => {
                    props.click && props.click(e, props.grid, props);
                }
            },
            icon && _("i", { className: icon }),
            icon && ' ',
            props.text
        );
    }
    props.autoComplete = "off";
    props.key = "Ikey" + props.index;
    return _(props.$ || Input, Object.assign({}, props, {$: undefined }));
};
/**
 * a Component to render tabular detail grid
 * @param {*} props
 */
const XShowDetailTabs = ({
    row,
    currentDetailGrids,
    parentGrid,
    topParentGrid
}) => {
    const [activeTab, setActiveTab] = React.useState(
        currentDetailGrids[0].grid.gridId
    );
    return _(
        Row,
        null,
        _(
            Col, { className: "border-0" },
            _(
                Nav, { tabs: true, className: 'detailTab-nav-tabs' },
                (currentDetailGrids || []).map(({ grid }, index) =>
                    _(
                        NavItem, { key: "NavItem" + index },
                        _(
                            NavLinkS, {
                                className: classNames('detailTab-nav-link', { active: activeTab === grid.gridId }),
                                onClick: () => {
                                    setActiveTab(grid.gridId);
                                }
                            },
                            grid.name
                        )
                    )
                )
            ),
            _(
                TabContent, { activeTab, className: "shadow-none" },
                (currentDetailGrids || []).map(({ grid, pk, params, detailGrids }) => {
                    var currentDetailGridProps = Object.assign({},  { pk: pk || {} }, grid );
                    if (currentDetailGridProps._url) {
                        currentDetailGridProps._url += buildParams2(params, row);
                    } else {
                        currentDetailGridProps.rows =
                            row[currentDetailGridProps.detailRowsFieldName];
                    }
                    currentDetailGridProps.detailFlag = true;
                    var { extraButtons, crudFlags } = grid;
                    return _(
                        TabPane, {
                            key: "TabPane" + grid.gridId,
                            tabId: grid.gridId,
                            className: "p-3"
                        },
                        _(
                            'div', {style:{marginLeft:-20}},
                            _(
                                Col, { md: "12" },
                                _(
                                    CardHeader, { className: 'pt-0 pb-1' },
                                    crudFlags &&
                                    crudFlags.insert &&
                                    _(
                                        Button, {
                                        //    className: "tlb-button ml-1",
                                            color: "primary", title:grid.newRecordLabel || getLocMsg("new_record"),
                                            onClick: event =>
                                                topParentGrid.onOnNewRecord(event, grid, row)
                                        },
                                        _("i", { className: "fa fa-plus" }),
                                        //getLocMsg("new_record")
                                        //_("i", { className: "icon-plus" }),
                                    ),
                                    (extraButtons || []).map((btn, index) =>
                                        _(XToolbarItem, Object.assign({}, btn,
                                            {index,
                                            row,
                                            grid,
                                            parentGrid,
                                            parentCt: topParentGrid
                                        }))
                                    )
                                )
                            )
                        ),
                        _(XGrid, Object.assign({
                            key: "XGrid" + grid.gridId,
                            responsive: true,
                            openTab: topParentGrid.props.openTab,
                            showDetail: detailGrids ?
                                currentDetailGridProps._detailTab ?
                                topParentGrid.showDetail3(
                                    detailGrids,
                                    currentDetailGridProps
                                ) :
                                topParentGrid.showDetail2(
                                    detailGrids,
                                    currentDetailGridProps
                                ) :
                                false,
                        },currentDetailGridProps
                        ))
                    );
                })
            )
        )
    );
};
/**
 *  a Component to render timebadge detail grid
 * @param {*} props
 */
const XShowDetailTimeline = ({
    row,
    currentDetailGrids,
    parentGrid,
    topParentGrid
}) => {
    return _(
        "ul", { className: "timeline" },
        (currentDetailGrids || []).map(
            ({ grid, pk, params, detailGrids }, index) => {
                if (parentGrid.state !== undefined && parentGrid.state["dg-" + grid.gridId] === false) return;
                var currentDetailGridProps = Object.assign({ pk: pk || {} }, grid );
                if (currentDetailGridProps._url) {
                    currentDetailGridProps._url += buildParams2(params, row);
                } else {
                    currentDetailGridProps.rows =
                        row[currentDetailGridProps.detailRowsFieldName];
                }
                currentDetailGridProps.detailFlag = true;
                var addBtnClick = currentDetailGridProps => event => {
                    if (grid._timelineBadgeBtn) {
                        return grid._timelineBadgeBtn(
                            event,
                            row,
                            currentDetailGridProps,
                            parentGrid,
                            topParentGrid
                        );
                    }
                    topParentGrid.onOnNewRecord(event, currentDetailGridProps, row);
                };

                return _(
                    "li", { key: "TimelinePane" + grid.gridId, className: "timeline-inverted" }, !currentDetailGridProps._hideTimelineBadgeBtn &&
                    _(
                        "div", {
                            className: "timeline-badge hover-shake " +
                                dgColors[index % dgColors.length],
                            onClick: addBtnClick(currentDetailGridProps),
                            style: { cursor: "pointer" }
                        },
                        _("i", { className: "fa fa-plus", style: { fontSize: 19 } })
                    ),

                    _(
                        "div", Object.assign({
                            className: "timeline-panel"},
                            !!currentDetailGridProps._hideTimelineBadgeBtn ?
                                { style: { left: "30px" } } :
                                {}
                        ),
                        _(
                            "div", { className: "timeline-heading mb-1" },
                            _(
                                "span", { className: "timeline-title pr-3 h5" },
                                currentDetailGridProps.name
                            ),
                            currentDetailGridProps.extraButtons &&
                            currentDetailGridProps.extraButtons.map((btn, index) =>
                                _(XToolbarItem, Object.assign({}, btn,
                                    {index,
                                    row,
                                    grid: currentDetailGridProps,
                                    parentGrid,
                                    parentCt: this
                                }))
                            )
                        ),
                        _(XGrid, Object.assign({
                            key: "XGrid" + grid.gridId,
                            responsive: true,
                            openTab: topParentGrid.props.openTab,
                            showDetail: detailGrids ?
                                currentDetailGridProps._detailTab ?
                                topParentGrid.showDetail3(
                                    detailGrids,
                                    currentDetailGridProps
                                ) :
                                topParentGrid.showDetail2(
                                    detailGrids,
                                    currentDetailGridProps
                                ) :
                                false,
                        },currentDetailGridProps
                        ))
                    )
                );
            }
        )
    );
};
/**
 * @description used for rendering master grid with search form in it
 * @param {Object}
 *            props - props of the grid
 * @param {Array}
 *            props.columns - props of the grid
 * @param {string}
 *            props.columns[].title - Ui title of the grid
 * @param {string}
 *            props.columns[].name - column name of the sql tale
 * @param {Boolean}
 *            props.columns[].sort - is it sortable column?
 * @param {Number}
 *            props.columns[].width - width of the column
 * @param {Function}
 *            props.columns[].formatter - a function to make own UI from the
 *            backend params (row,cell)
 * @param {Object}
 *            props.crudFlags - An object to make UI ACL {insert: true, edit:
 *            true, remove: true}
 * @param {Number}
 *            props.crudFormId - An Id of the Form
 * @param {Number}
 *            props.crudTableId - SQL table id
 * @param {Number}
 *            props.defaultHeight -
 * @deprecated defaultHeight is a height of the Grid
 * @param {Number}
 *            props.defaultWidth -
 * @deprecated defaultWidth is width of the Grid
 * @param {Array}
 *            props.detailGrids[] - ['false']=> no grid, Array of detail grids
 *            conf
 * @param {Object}
 *            props.detailGrids[].grid - detail grids props
 * @param {Object}
 *            props.detailGrids[].params - master detail connection Master
 *            primaty key name {xoffer_id: "offer_id"}
 * @param {Object}
 *            props.detailGrids[].pk - Master detail connection Detail primaty
 *            key name {toffer_detail_id: "offer_detail_id"}
 * @param {Number}
 *            props.gridId - Id of the grid
 * @param {string}
 *            props.gridReport - show or not show reporter tools
 * @param {string}
 *            props.keyField - PK of the table
 * @param {string}
 *            props.name - UI Name of the grid table
 * @param {
 *            Array } props.menuButtons - return array of Objects conf { text,
 *            handler, cls, ref }
 * @param {Number}
 *            props.pageSize - Number of rows in grid to show in one page
 * @param {Number}
 *            props.queryId - Query id of the Grid
 * @param {Symbol}
 *            props.searchForm - Search form is generated from ServerSide and
 *            extens from XForm Component
 * @param {String}
 *            props._url -
 *            ["ajaxQueryData?_renderer=react16&.t=tpi_1531758063549&.w=wpi_1531758063547&_qid=4220&_gid=3376&firstLimit=10"]
 * @param {function}
 *            props._timelineBadgeBtn - will work when the timelineBadge is
 *            clicked
 * @param {Number}
 *            props.forceRelaod - to find out weathet it is delated or not used
 *            to compare props with prevProps
 * @param {Boolean}
 *            props._hideTimelineBadgeBtn - to hide _hideTimelineBadgeBtn
 * @param {Array}
 *            props.extraButtons - Array of buttons in grid
 *
 */
class XMainGrid extends GridCommon {
    constructor(props) {
        super(props);
        var oldGridState = iwb.grids[props.id];
        if (iwb.debug) console.log("XMainGrid", props);
        if(props.setCmp)props.setCmp(this);
        if (oldGridState) {
            this.state = oldGridState;
            this.dontRefresh = true; // true-yuklemez, false-yukleme yapar
        } else {
            var columns = [],
                columnExtensions = [];
            const canIOpenActions =
                (props.crudFlags && (props.crudFlags.edit || props.crudFlags.remove)) ||
                props.menuButtons;

            var colTemp = props.columns;
            colTemp &&
                colTemp.map(colLocal => {
                	if(!colLocal.hidden){
	                    var title;
	                    switch (colLocal.name) {
	                        case "pkpkpk_faf":
	                            title = _("i", {
	                                className: "icon-paper-clip"
	                            });
	                            break;
	                        case "pkpkpk_ms":
	                            title = _("i", {
	                                className: "icon-envelope"
	                            });
	                            break;
	                        case "pkpkpk_cf":
	                            title = _("i", {
	                                className: "icon-bubble"
	                            });
	                            break;
	                        case "pkpkpk_apf":
	                            title = _("i", {
	                                className: "icon-picture"
	                            });
	                            break;
	                        case "pkpkpk_vcsf":
	                            title = _("i", {
	                                className: "icon-social-github"
	                            });
	                            break;
	                    }
	                    columns.push({
	                        name: colLocal.name,
	                        title: title || colLocal.title,
	                        getCellValue: colLocal.formatter || undefined
	                    });
	                    columnExtensions.push({
	                        columnName: colLocal.name,
	                        align: colLocal.align || "left",
	                        width: +colLocal.width,
	                        sortingEnabled: !!colLocal.sort
	                    });
                	}
                });
            if (canIOpenActions) {
                columns.push({
                    name: "_qw_",
                    title: ".",
                    getCellValue: rowData => {
                        var { onEditClick, onDeleteClick } = this;
                        return _(XGridRowAction2, Object.assign(
                        		{ rowData, menuButtons: props.menuButtons
                        		,formSmsMailList: props.formSmsMailList, crudFlags: props.crudFlags
                        		,onEditClick, onDeleteClick,
                        		parentCt: this }
                        ));
                    }
                });
                columnExtensions.push({
                    columnName: "_qw_",
                    width: 80,
                    align: "center",
                    sortingEnabled: false
                });
            }
            var state = {
                columns,
                order: columns.map(({ name }) => name),
                columnExtensions,
                columnWidths: columnExtensions.map(({ columnName, width }) => {
                    return { columnName, width };
                }),
                rows: [],
                sorting: [],
                totalCount: 0,
                pageSize: props.pageSize,
                pageSizes: props.pageSize > 1 ?
                    [] ://parseInt(props.pageSize / 2), props.pageSize, 3 * props.pageSize
                    [5, 10, 25, 100],
                currentPage: 0,
                hideSF: !props.showSF,
                loading: false
            };
            props.detailGrids &&
                props.detailGrids.length > 1 &&
                props.detailGrids.map(({ grid }, key) => {
                    if (key < 2) state["dg-" + grid.gridId] = key < 2;
                });
            this.state = state;
        }
        /**
         * used to give click event to the detail timeLineBadge button
         * (event,masterDridProps,detailGridProps,row)
         */
        this._timelineBadgeBtn = this.props._timelineBadgeBtn;
        /**
         * @description A function to open and close detail grid
         * @param {event}
         *            event - click event
         * @param {Object}
         *            event.target - target object from clicked place
         */
        this.toggleDetailGrid = ({ target }) => {
            this.setState(Object.assign({}, this.state, {[target.name]: target.checked }));
        };
        let { searchForm, detailGrids } = this.props;
        if (searchForm || (detailGrids && detailGrids.length > 1)) {
            var self = this;
            this.searchForm = _(
                Nav, { style: {} },
                searchForm &&
                _(
                    "span",
                    null,
                    _(
                        "div", { className: "hr-text" },
                        _("h6", null, getLocMsg("search_criteria"))
                    ),
                    _(
                        "div", { style: { zoom: ".9" }, className: "searchFormFields" },
                        _(searchForm, { parentCt: this }),
                        _(
                            "div", { className: "form-group", style: { paddingTop: 10 } },
                            _(
                                Button, {
                                    color: "danger",
                                    style: { width: "100%", borderRadius: 2 },
                                    onClick: () => {
                                        this.loadData(true);
                                    }
                                },
                                getLocMsg("search")
                            )
                        )
                    ),
                    /*
                     * _("div", { style: { height: 10 } }), _("div", { className:
                     * "hr-text" }, _("h6", null, "Şablonlar")), _( Link, { style: {
                     * padding: 2 }, to: "" }, _("i", { className: "icon-star" }), " ", "
                     * Yıllık Faturalar" ), _( Link, { style: { padding: 2, color:
                     * "#a0a0a0" }, to: "" }, _("i", { className: "icon-plus" }), " ", "
                     * Yeni Şablon Ekle" ),
                     */
                    _("div", { style: { height: 20 } })
                ), !this.props._detailTab &&
                detailGrids &&
                detailGrids.length > 1 &&
                _(
                    "div", { className: "hr-text", key: "hr-text" },
                    _("h6", null, getLocMsg("details"))
                ), !this.props._detailTab &&
                detailGrids &&
                detailGrids.length > 1 &&
                detailGrids.map((detailGrid, key) => {
                    return _(
                        "div", { key, className: "py-1 pr-0 pl-1 text-dark" },
                        detailGrid.grid.name,
                        _(
                            "label", {
                                className: "float-right switch switch-xs switch-3d switch-" +
                                    dgColors[key % dgColors.length] +
                                    " form-control-label"
                            },
                            _("input", {
                                name: "dg-" + detailGrid.grid.gridId,
                                type: "checkbox",
                                className: "switch-input form-check-input",
                                onChange: self.toggleDetailGrid,
                                defaultChecked: self.state["dg-" + detailGrid.grid.gridId]
                            }),
                            _("span", { className: "switch-label" }),
                            _("span", { className: "switch-handle" })
                        )
                    );
                })
            );
        }
        /**
         * @description A function to search globally
         * @param {Event}
         *            event - event from the global search
         */
        this.onGlobalSearch = event =>
            this.loadData(true, {
                xsearch: event && event.target ? event.target.value : event
            });
        iwb.onGlobalSearch2 = this.onGlobalSearch;
        /**
         * @description Is a function to toggle search form from the XMainGrid
         *              component and animata iconMagnifier
         */
        this.toggleSearch = event => {
            event.preventDefault();
            event.stopPropagation();
            this.setState({ hideSF: !this.state.hideSF });
        };
        /**
         * @description A function to open EXPORT menu in XModal
         */
        this.openBI = () => {
            let { props } = this;
            let { columnExtensions, order } = this.state;
            let cmap = {};
            let url = "grd/" + props.name + ".";
            let params = "?_gid=" + props.gridId + "&_columns=";
            columnExtensions.map(({ columnName, width }) => {
                cmap[columnName] = width;
            });
            order.map(
                columnName =>
                (params += columnName + "," + (cmap[columnName] || 100) + ";")
            );
            if (this.form) {
                var vals = this.form.getValues()
                if (vals)
                    for (var k in vals)
                        if (vals[k] != '') params += '&' + k + '=' + vals[k];
            }
            iwb.showModal({
                title: "REPORTS / BI",
                footer: false,
                color: "danger",
                size: "sm",
                body: _(
                    ListGroup, {
                        style: {
                            fontSize: "1.0rem"
                        }
                    },
                    _("b", null, "Exports"),
                    _(
                        ListGroupItem, {
                            tag: "a",
                            href: url + "xls" + params,
                            target: "_blank",
                            action: true
                        },
                        _("i", {
                            className: "float-right text-success fa fa-file-excel"
                        }),
                        "Excel"
                    ),
                    _(
                        ListGroupItem, {
                            tag: "a",
                            href: url + "pdf" + params,
                            target: "_blank",
                            action: true
                        },
                        _("i", {
                            className: "float-right text-danger fa fa-file-pdf"
                        }),
                        "PDF"
                    ),
                    _(
                        ListGroupItem, {
                            tag: "a",
                            href: url + "csv" + params,
                            target: "_blank",
                            action: true
                        },
                        _("i", {
                            className: "float-right text-secondary fa fa-file-alt"
                        }),
                        "CSV"
                    ),
                    _(
                        ListGroupItem, {
                            tag: "a",
                            href: url + "txt" + params,
                            target: "_blank",
                            action: true
                        },
                        _("i", {
                            className: "float-right text-secondary fa fa-file-word"
                        }),
                        "Text"
                    ),
                    _("hr"),
                    _("b", null, "BI"),
                    _(
                        ListGroupItem, {
                            tag: "a",
                            href: "showPage?_tid=" +
                                (props.crudTableId ?
                                    "1200&xtable_id=" + props.crudTableId :
                                    "2395&xquery_id=" + props.queryId),
                            target: "_blank",
                            action: true /* , className:'list-group-item-danger2' */
                        },
                        _("i", {
                            className: "float-right text-primary fa fa-th"
                        }),
                        "Pivot Table"
                    ),
                    _(
                        ListGroupItem, {
                            tag: "a",
                            href: "showPage?_tid=" +
                                (props.crudTableId ?
                                    "784&xtable_id=" + props.crudTableId :
                                    "2413&xquery_id=" + props.queryId),
                            target: "_blank",
                            action: true
                        },
                        _("i", {
                            className: "float-right text-primary fa fa-table"
                        }),
                        "Data List"
                    )
                )
            });
        };

        /**
         * @description A function to render Details under Muster's row
         * @param {Array}
         *            currentDetailGrids[] - array of detail grids conf
         * @param {Object}
         *            currentDetailGrids[].grid - detail grids props
         * @param {Object}
         *            currentDetailGrids[].params - master detail connection Master
         *            primaty key name {xoffer_id: "offer_id"}
         * @param {Object}
         *            currentDetailGrids[].pk - Master detail connection Detail primaty
         *            key name {toffer_detail_id: "offer_detail_id"}
         */
        this.showDetail2 = (currentDetailGrids, parentGrid = this) => ({ row }) =>
            row ?
            _(XShowDetailTimeline, {
                row,
                currentDetailGrids,
                parentGrid,
                topParentGrid: this
            }) :
            null;
        /**
         * tabular master detail layout
         */
        this.showDetail3 = (currentDetailGrids, parentGrid = this) => ({ row }) =>
            row ?
            _(XShowDetailTabs, {
                row,
                currentDetailGrids,
                parentGrid,
                topParentGrid: this
            }) :
            null;
        /**
         * @overloading
         * @param {Boolean}
         *            force - Get up to data data
         * @param {object}
         *            params -[{xsearch:'searchValue'}] Params from Global Search
         */
        this.loadData = (force, params = {}, dashIgnore) => {
            if (!iwb.loadGrid[this.props.id]) iwb.loadGrid[this.props.id] = this.loadData;
            const queryString = this.queryString();
            if (!force && queryString === this.lastQuery) {
                return;
            }
            var tempParams = Object.assign({}, (this.form ? this.form.getValues() : {}),
                params);
            if(this.props.beforeLoad && this.props.beforeLoad(this, tempParams)===false)return;
            
            this.setState({ loading: !0 });
            var self = this;
            iwb.request({
                url: queryString,
                self: this,
                params: tempParams,
                successCallback: (result, cfg) => {
                	if(cfg.self.props.onLoad && cfg.self.props.onLoad(result, cfg)===false)return;
                    cfg.self.setState({
                        rows: result.data,
                        loading: false,
                        totalCount: result.total_count
                        , extraOutMap: result.extraOutMap||{}
                    });

                    if (cfg.self.props.summary) cfg.self.props.summary.map((ox) => {
                        if (ox.graphId) {
                            if (!dashIgnore) {
                                var gox = ox;
                                iwb.graph(ox, 'gr-' + cfg.self.props.id + '-' + ox.graphId, (selValue) => {
                                    var xgroupBy = gox.groupBy.split('.');
                                    var groupBy = 'x' + xgroupBy[xgroupBy.length - 1];
                                    var xparams = {};
                                    if (selValue !== false) xparams[groupBy] = selValue;
                                    this.loadData(!0, xparams, !0);

                                });
                            }
                        } else iwb.request({
                            url: 'ajaxQueryData?_qid=' + ox + '&.r=' + Math.random(),
                            self: cfg.self,
                            params: tempParams,
                            successCallback: (result2, cfg2) => {
                                if (!result2.data.length) return;
                                var j = result2.data[0];
                                var options = {
                                    chart: {
                                        height: 300,
                                        type: 'radialBar',
                                    },
                                    plotOptions: {
                                        radialBar: {
                                            startAngle: -135,
                                            endAngle: 225,
                                            hollow: {
                                                margin: 0,
                                                size: '70%',
                                                background: '#fff',
                                                image: undefined,
                                                imageOffsetX: 0,
                                                imageOffsetY: 0,
                                                position: 'front',
                                                dropShadow: {
                                                    enabled: true,
                                                    top: 0,
                                                    left: 0,
                                                    blur: 4,
                                                    opacity: 0.24
                                                }
                                            },
                                            dataLabels: {
                                                showOn: 'always',
                                                name: {
                                                    offsetY: -10,
                                                    show: true,
                                                    color: '#888',
                                                    fontSize: '17px'
                                                },
                                                value: {
                                                    formatter: function(val) {
                                                        return j.val;
                                                    },
                                                    color: '#111',
                                                    fontSize: '36px',
                                                    show: true,
                                                }
                                            }
                                        }
                                    },
                                    series: [100 * j.xval],
                                    stroke: {
                                        lineCap: 'round'
                                    },
                                    labels: [j.title],

                                }

                                var xid = 'ga-' + cfg2.self.props.id + '-' + ox;
                                if (iwb.charts[xid]) iwb.charts[xid].destroy();
                                var chart = new ApexCharts(
                                    document.getElementById(xid),
                                    options
                                );
                                iwb.charts[xid] = chart;

                                chart.render();
                            }
                        });

                    })


                },
                errorCallback: (error, cfg) => {
                    cfg.self.setState({
                        rows: [],
                        totalCount: 0,
                        loading: false, extraOutMap:{}
                    });
                }
            });
            this.lastQuery = queryString;
        };
    }
    componentDidMount() {
        if (!this.dontRefresh) this.loadData();
        this.dontRefresh = false;
    }
    componentDidUpdate(prevProps, prevState, snapshot) {
        if (this.props.forceRelaod !== prevProps.forceRelaod) {
            this.loadData(true);
        } else {
            this.loadData();
            this.dontRefresh = false;
        }
    }
    componentWillUnmount() {
        var state = Object.assign({}, this.state);
        var sf = document.getElementById("sf-" + this.props.id);
        if (sf) {
            state.hideSF = sf.classList.contains("sf-hidden");
        }
        iwb.grids[this.props.id] = state;
    }
    render() {
        const {
            state: {
                rows,
                order,
                hideSF,
                columns,
                sorting,
                loading,
                pageSize,
                selection,
                pageSizes,
                totalCount, extraOutMap,
                currentPage,
                columnWidths,
                columnExtensions
            },
            props: {
                tree,
                keyField,
                crudFlags,
                detailGrids,
                multiselect,
                extraButtons,
                treeParentKey,
                titleComponent,
                tableTreeColumn,
                groupColumn, displayInfo, displayAgg,
                _disableSearchPanel,
                _disableIntegratedSorting,
                _disableIntegratedGrouping
            },
            loadData,
            searchForm,
            rowComponent,
            toggleSearch,
            onOrderChange,
            onOnNewRecord,
            onSortingChange,
            onPageSizeChange,
            onSelectionChange,
            onCurrentPageChange,
            onColumnWidthsChange
        } = this;

        let showDetail = detailGrids && detailGrids.length > 0;
        let grid = _(
            _dxgrb.Grid, {
                className: "maingrid",
                rows: rows,
                columns,
                getRowId: row => row[keyField]
            },
            /** sorting state */
            !_disableIntegratedSorting &&
            _(
                _dxrg.SortingState, !pageSize ? null : { sorting, onSortingChange, columnExtensions }
            ),
            multiselect &&
            _(_dxrg.SelectionState, {
                selection,
                onSelectionChange
            }), !!tree && _(_dxrg.TreeDataState), !!tree &&
            _(_dxrg.CustomTreeData, {
                getChildRows: (row, rootRows) => {
                    const childRows = rootRows.filter(
                        r => r[treeParentKey] == (row ? row[keyField] : "0")
                    );
                    return childRows.length ? childRows : null;
                }
            }),
            /** pagesize > 0 will import search state */
            !pageSize ? _(_dxrg.SearchState, null) : null,
            /** Client filtering */
            !_disableSearchPanel &&
            !pageSize &&
            rows.length > 1 &&
            _(_dxrg.IntegratedFiltering, null),
            /** state of the grouping */
            (groupColumn || (!_disableIntegratedGrouping &&
            !pageSize &&
            rows.length > 1)) &&
            _(_dxrg.GroupingState, groupColumn ? {defaultGrouping:[{ columnName: groupColumn }]}:{}),
            /** ability to group like a tree */

            (groupColumn || (!_disableIntegratedGrouping &&
            !pageSize &&
            rows.length > 1)) &&
            _(_dxrg.IntegratedGrouping, null),
            /** sorting wii be enabled when pageSize>0 and row has more than one data */
            !_disableIntegratedSorting &&
            !pageSize &&
            rows.length > 1 &&
            _(_dxrg.IntegratedSorting, null),
            /** row detail state */
            showDetail ? _(_dxrg.RowDetailState, null) : null,
            /** state paging */
            rows.length > iwb.detailPageSize || pageSize > 1 ?
            _(
                _dxrg.PagingState,
                pageSize > 1 ?
                { pageSize, currentPage, onPageSizeChange, onCurrentPageChange } :
                {}
            ) :
            null,
            multiselect && _(_dxrg.IntegratedSelection, null),
            /** For remote paging */
            pageSize > 1 &&
            rows.length > 1 &&
            _(_dxrg.CustomPaging, { totalCount: totalCount }),
            /** enable group drag drop */
            _(_dxgrb.DragDropProvider, null),
            /** ui table */
            _(_dxgrb.Table, {
                columnExtensions,
                rowComponent,
                messages: { noData: getLocMsg("no_data") }
            }),
            /** multiselect */
            multiselect &&
            _(_dxgrb.TableSelection, {
                showSelectAll: true
            }),
            /** UI ordering of the table */
            _(_dxgrb.TableColumnReordering, { order, onOrderChange }),
            /** UI tablle resizing */
            _(_dxgrb.TableColumnResizing, { columnWidths, onColumnWidthsChange }),
            /** UI to show table row container */
            _(_dxgrb.TableHeaderRow, {
                showSortingControls: true,
                titleComponent: titleComponent || _dxgrb.TableHeaderRow.Title
            }),
            /** tree support */
            !!tree && _(_dxgrb.TableTreeColumn, { for: tableTreeColumn }),
            /** UI of the detail table */
            showDetail ?
            _(_dxgrb.TableRowDetail, {
                contentComponent: this.props._detailTab ?
                    this.showDetail3(detailGrids) :
                    this.showDetail2(detailGrids)
            }) :
            null,
            /** UI show pagining */
            pageSize > 1 //rows.length > iwb.detailPageSize ||
            ?
            _(_dxgrb.PagingPanel, { pageSizes: pageSizes }) //|| iwb.detailPageSize
            :
            null,
            /** UI table Grouping */
            (groupColumn || (!_disableIntegratedGrouping && !pageSize && rows.length > 1)) ?
            _(_dxgrb.TableGroupRow, null) :
            null,
            /** top of grit do render some buttons */
            (groupColumn || (!pageSize && rows.length > 1)) && _(_dxgrb.Toolbar, null),
            /** ui search input */
            !pageSize &&
            rows.length > 1 &&
            !_disableSearchPanel &&
            _(_dxgrb.SearchPanel, {
                messages: { searchPlaceholder: getLocMsg("search_placeholder") },
                changeSearchValue: ax => {
                    if (iwb.debug) console.log("onValueChange", ax);
                }
            }),
            /** UI grouping panel */
            (groupColumn || (!_disableIntegratedGrouping &&
            !tree && !pageSize &&
            rows.length > 1)) &&
            _(_dxgrb.GroupingPanel, {
                showSortingControls: true,
                messages: { groupByColumn: getLocMsg("group_by_column") }
            })
        );

        return _(
            "div", { className: "tab-grid mb-4" },
            searchForm &&
            _(
                "nav", {
                    id: "sf-" + this.props.id,
                    className: classNames({ "sf-hidden": hideSF })
                },
                searchForm
            ),
            _(
                "main", { className: "inbox" },
                _(
                    CardHeader, { className: 'pb-1' },
                    searchForm &&
                    !this.props.showSF && _(
                        Button, {
                            className: "tlb-button ml-1",
                            color: "secondary",
                            onClick: toggleSearch
                        },
                        _("i", {
                            id: "eq-" + this.props.id,
                            className: classNames("icon-magnifier ", {
                                "rotate-90deg": !hideSF
                            })
                        })
                    ), !this.props.showSF && _(
                        Button, {
                            className: "tlb-button ml-1",
                            disabled: loading, 
                            color: "secondary",
                            onClick: event => loadData(true)
                        },
                        _("i", { className: "icon-refresh " + (this.state.loading ? " infinite-rotate" : "") })
                    ),
                    displayInfo && this.state.totalCount>0 && _('div',{style:{padding:6}}, getLocMsg("total"),' ', _('span',{style:{borderRadius:100, background:'rgba(206, 243, 213, 0.8)', padding:'2px 5px'}},this.state.totalCount), ' ', getLocMsg("records")),
                    displayAgg && this.state.totalCount>0 && _('div',{style:{display: 'inline-flex'}}, displayAgg.map(oo=> {
                    	return _('div',{style:{padding:5, marginLeft:10, borderBottom: '1px solid #e8e8e8'}},oo.f(extraOutMap[oo.id]));
                    	})),
                    
                    !!iwb.newRecordPositionRight && _("div", { className: "fgrow" }),

                    crudFlags &&
                    crudFlags.insert &&
                    _(
                        Button, {
                            className: "tlb-button ml-1",style:iwb.newRecordPositionRight ? {borderRadius:50}:{},
                            color: "primary",
                            onClick: event => onOnNewRecord(event, this.props)
                        },
                        //_("i", { className: "fa fa-plus" }),
                        //"Yeni Kayit"
                        !!iwb.newRecordPositionRight && _('i',{className:'fa fa-plus'}),
                        !!iwb.newRecordPositionRight && ' ',
                        this.props.newRecordLabel || getLocMsg("new_record")
                    ),
                    !iwb.newRecordPositionRight && _("div", { className: "fgrow" }),
                    extraButtons &&
                    extraButtons.map((btn, index) =>
                        _(XToolbarItem, Object.assign({}, btn,
                            {index,
                            row: null,
                            grid: this,
                            parentCt: null
                        }))
                    ),
                    // _(Button,{className:'float-right btn-round-shadow
                    // hover-shake',color:'danger',
                    // onClick:this.toggleSearch},_('i',{style:{transition: "transform
                    // .2s"},id:'eq-'+this.props.id,className:'icon-equalizer'+(this.state.hideSF?'':'
                    // rotate-90deg')}))
                    this.props.gridReport &&
                    _(
                        Button, {
                            className: "float-right tlb-button mx-1",
                            color: "secondary", style:{color:"red"},
                            onClick: this.openBI
                        },
                        _("i", { className: "icon-equalizer" })
                    ) // , this.props.globalSearch && _(Input,{type:"text",
                    // className:"float-right form-control w-25",
                    // onChange:this.onGlobalSearch, placeholder:"Hızlı Arama...",
                    // defaultValue:"", style:{marginTop: '-0.355rem',
                    // marginRight:'.4rem'}}) )
                ),
                grid
            ),
            this.props.summary && _('summary', {}, this.props.summary.map((ox) => {
                return _('div', { id: ox.graphId ? ('gr-' + this.props.id + '-' + ox.graphId) : ('ga-' + this.props.id + '-' + ox) }, )
            }))
        );
    }
}
XMainGrid.defaultProps = {
    tree: false,
    treeParentKey: "parent_id",
    tableTreeColumn: "dsc"
};
class XMainCard extends GridCommon {
    constructor(props) {
        if (iwb.debug) console.log("XMainCard", props);
        super(props);
        if(props.setCmp)props.setCmp(this);
        this.state = {
            totalCount: 0,
            pageSize: props.pageSize,
            pageSizes: props.pageSize > 1 ?
                [] ://parseInt(props.pageSize / 2), props.pageSize, 3 * props.pageSize
                [5, 10, 25, 100],
            currentPage: 1,
            hideSF: true, sort:'',dir:'',
            loading: false,
            cards: [],
            breakPoints: props.breakPoints || [300, 500, 1000],
            xsearch:'',
            xtags:[]//name, value, label, className
        };
        let { searchForm, detailGrids } = this.props;
        if (searchForm || (detailGrids && detailGrids.length > 1)) {
            var self = this;
            this.searchForm = _(
                Nav, {},
                searchForm &&
                _(
                    "span", {},
                    _(
                        "div", { className: "hr-text" },
                        _("h6", null, getLocMsg("search_criteria"))
                    ),
                    _(
                        "div", { style: { zoom: ".9" }, className: "searchFormFields" },
                        _(searchForm, { parentCt: this }),
                        _(
                            "div", { className: "form-group", style: { paddingTop: 10 } },
                            _(
                                Button, {
                                    color: "danger",
                                    style: { width: "100%", borderRadius: 2 },
                                    onClick: event => {
                                        event.preventDefault();
                                        event.stopPropagation();
                                        this.loadData(true);
                                    }
                                },
                                getLocMsg("search")
                            )
                        )
                    ),
                    _("div", { style: { height: 20 } })
                ),
                detailGrids &&
                detailGrids.length > 1 &&
                _(
                    "div", { className: "hr-text", key: "hr-text" },
                    _("h6", null, getLocMsg("details"))
                ),
                detailGrids &&
                detailGrids.length > 1 &&
                detailGrids.map((detailGrid, key) => {
                    return _(
                        "div", {
                            key,
                            style: {
                                padding: "3px 0px 2px 3px",
                                color: "#6d7284",
                                fontSize: ".9rem"
                            }
                        },
                        detailGrid.grid.name,
                        _(
                            "label", {
                                className: "float-right switch switch-xs switch-3d switch-" +
                                    dgColors[key % dgColors.length] +
                                    " form-control-label"
                            },
                            _("input", {
                                name: "dg-" + detailGrid.grid.gridId,
                                type: "checkbox",
                                className: "switch-input form-check-input",
                                onChange: self.toggleDetailGrid,
                                defaultChecked: self.state["dg-" + detailGrid.grid.gridId]
                            }),
                            _("span", { className: "switch-label" }),
                            _("span", { className: "switch-handle" })
                        )
                    );
                })
            );
        }
        /**
         * @description prerpares url with query
         * @example Used in XMainGrid and XGrid to make query inside loadData
         * @returns {String}
         */
        this.queryString = () => {
            const { pageSize, currentPage, sort, dir } = this.state;
            let queryString =
                this.props._url +
                "&limit=" +
                pageSize +
                "&start=" +
                pageSize * (currentPage - 1);
            if(sort)queryString+='&sort='+sort;
            if(dir)queryString+='&dir='+dir;
            if(this.props.xsearch)queryString+='&'+this.props.xsearch+'='+(this.state.xsearch||'');
        	var xtags = this.state.xtags;
            if(xtags.length){
            	for(var qi=0;qi<xtags.length;qi++)
            		queryString+='&'+xtags[qi].name+'='+(xtags[qi].value||'');
            }
            return queryString;
        };
        /** toogle search */
        this.toggleSearch = event => {
            event.preventDefault();
            event.stopPropagation();
            this.setState({
                hideSF: !this.state.hideSF
            });
        };
        
        /** loadData */
        this.loadData = force => {
            if (this.props.cards || this.state.loading) return;
            const queryString = this.queryString();
            if (!force && queryString === this.lastQuery) {
                return;
            }
            this.setState({ xcards: [], loading: true });
            iwb.request({
                url: queryString,
                self: this,
                params: Object.assign({}, (this.form ? this.form.getValues() : {}) ),
                successCallback: (result, cfg) => {
                    cfg.self.setState({
                        cards: result.data,
                        totalCount: result.total_count,
                        loading: false
                    });
                },
                errorCallback: (error, cfg) => {
                    cfg.self.setState({
                        cards: [],
                        totalCount: 0,
                        loading: false
                    });
                }
            });
            this.lastQuery = queryString;
        };
        /**rightClick on the card */
        this.addTag = tag => {
        	if(!tag || !tag.name)return;
        	var xtags = this.state.xtags;
        	for(var qi=0;qi<xtags.length;qi++){
        		if(xtags[qi].name==tag.name && xtags[qi].value==tag.value)
        			return;
        	}
        	xtags.push(tag)
        	this.setState({xtags:xtags});
        	setTimeout(()=>this.loadData(!0),100);
        }
            
        /**rightClick on the card */
        this.RightClickComponent = props => extraProps =>
            _(XGridRowAction2, Object.assign({}, props, extraProps ));
            
        this.openOrderBy = (e)=>{
            e.preventDefault();
            e.stopPropagation();
        	var self = this;
        	iwb.showModal({
                title: getLocMsg("sort"),
                footer: false,
                color: "primary",
                size: "sm",
                body: _(
                    ListGroup, {
                        style: {
                            fontSize: "1.0rem"
                        }
                    },
                    this.props.orderNames.map((o)=>_(
                        ListGroupItem, {
                            tag: o.id==self.state.sort ?"b" : "span",
                            onClick:(oq)=>{
                            	var dir="ASC";
                            	if(self.state.sort == o.id && self.state.dir!="DESC")
                            		dir="DESC";
                            	self.setState({sort:o.id, dir:dir});
                            	setTimeout(()=>self.loadData(!0),10);
                            	iwb.closeModal();
                            },style:{cursor:'pointer'}
                        },
                        o.dsc,
                        (o.id==self.state.sort? _('icon',{style:{float:'right',fontSize: '1.4rem',color: '#999'}, className:self.state.dir=="DESC"?"icon-arrow-down-circle":"icon-arrow-up-circle"}):'')
                    ))
                )
            });
        	return false;
        }
    }
    shouldComponentUpdate(){
//    	console.log('shouldComponentUpdate');
    	return true;
    }
    componentDidMount() {
        if (!this.dontRefresh) this.loadData();
        this.dontRefresh = false;
    }
    render() {
        let {
            state: {
                cards,
                hideSF,
                loading,
                pageSize,
                pageSizes,
                totalCount,
                breakPoints,
                currentPage
            },
            props: {
                crudFlags,                
                menuButtons,
                extraButtons,
                formSmsMailList
            },
            loadData,
            searchForm,
            onEditClick,
            toggleSearch,
            onDeleteClick,
            onOnNewRecord,
            addTag
        } = this;
        return _(
            "div", { className: "tab-grid mb-4" },
            searchForm &&
            _(
                "nav", {
                    id: "sf-" + this.props.cardId,
                    className: classNames({ "sf-hidden": hideSF })
                },
                searchForm
            ),
            _("main", { className: "inbox" },
                _('div',{className:"card-page-header"},
	                _(
	                    CardHeader, {},
	                    searchForm &&
	                    _(
	                        Button, {
	                            className: "tlb-button ml-1",
	                            color: "secondary",
	                            onClick: toggleSearch
	                        },
	                        _("i", {
	                            id: "eq-" + this.props.cardId,
	                            className: classNames("icon-magnifier ", {
	                                "rotate-90deg": !hideSF
	                            })
	                        })
	                    ),
	                    _(
	                        Button, {
	                            className: "tlb-button ml-1",
	                            disabled: loading,
	                            color: "secondary",
	                            onClick: event => loadData(true)
	                        },
	                        _("i", { className: "icon-refresh" })
	                    ),
	                    crudFlags &&
	                    crudFlags.insert &&
	                    _(
	                        Button, {
	                            className: "tlb-button ml-1",
	                            color: "primary",
	                            onClick: event => onOnNewRecord(event, this.props)
	                        },
	                       // _("i", { className: "fa fa-plus" }),
	                        this.props.newRecordLabel || getLocMsg("new_record")
	                    ),
	                    extraButtons &&
	                    extraButtons.map((btn, index) =>
	                        _(XToolbarItem, Object.assign({}, btn,
	                            {index,
	                            row: null,
	                            grid: this,
	                            parentCt: null
	                        }))
	                    ),
	                    this.props.xsearch && _('input',{style:{float:'right'},onChange:(ev)=>{
	                    	var xsearch = ev.target.value||'';
	                    	this.setState({xsearch:xsearch});
	                    	var self = this;
	                    	if(!this.delayedTask)this.delayedTask=new iwb.delayedTask(function(p){
	                    		self.loadData(!0);
	                    	});
	                    	this.delayedTask.delay(200, xsearch);
	                    	
	                    }, value:this.state.xsearch, type:"text", className:"form-control w-25",placeholder:getLocMsg("search_placeholder")}),
	                    
	                    false && this.props.orderNames && _(
	                            Button, {
	                                className: "float-right tlb-button mx-1",
	                                color: "secondary", style:{color:"#607D8B"},
	                                onClick: this.openOrderBy, title:getLocMsg("sort")
	                            },
	                            _("i", { className: "icon-equalizer" })
	                        ),
	                    // this.props.gridReport &&
	                    // _( Button,
	                    //   { className: "float-right btn-round-shadow hover-shake mx-1",
	                    //     color: "danger",
	                    //     onClick: this.openBI
	                    //   },
	                    //   _("i", { className: "icon-equalizer" })
	                    // )
	                ),
	                (this.props.pageSize || this.props.orderNames) && _('div',{ style:{fontSize: '.8rem'
	                    ,color: '#999'}},
	                    _('span',{style:{float:'right'}}
	                    	,this.state.xtags.map(o=>_('div',{className:o.className||'badge badge-secondary'
	                    		, style:{fontSize: '.8rem', borderRadius:20, marginRight: 12}},_('i', {className:'icon-close', style:{cursor:'pointer'}, onClick:(e)=>{
	                            	var xtags = this.state.xtags;
	                            	for(var qi=0;qi<xtags.length;qi++){
	                            		if(xtags[qi].name==o.name && xtags[qi].value==o.value){
	                            			xtags.splice(qi,1);
	                            			this.setState({xtags:xtags});
	                            			setTimeout(()=>this.loadData(!0),100);
	                            			return;
	                            		}
	                            	} 
	                            }}), ' ', o.label)), 
	                    		this.props.pageSize>0 && (totalCount + ' records'+(this.props.orderNames?', ':''))
	                    		,' ',this.props.orderNames && (getLocMsg('sort')+': '),
	                    		this.props.orderNames && _('a',{href:'#', onClick:this.openOrderBy},(this.state.sort? (this.state.sort+' '+ this.state.dir):'(none)')))),
	                 this.props.pageSize && _(XPagination, {
	                        pageSize,
	                        currentPage,
	                        totalCount,
	                        onChange: currentPage =>
	                            this.setState({ currentPage }, () => this.loadData(true))
	                    }),
                    ),
                _(
                    XMasonry, {
                        breakPoints: breakPoints,
                        loadingComponent: () => _(XLoading, null),
                        // item: {
                        //   className: 'mt-2 mb-2 border-6px card-animated'
                        // },
                        rootInner: {
                            className: "d-flex flex-row justify-content-center align-content-stretch flex-fill m-auto w-100 p-2"
                        }
                    },
                    cards.map((record, index) => {
                        return this.props.render(Object.assign({}, record,
                            {parentCt: this,
                            key: index,
                            index: index, addTag:addTag,
                            RightClickComponent: this.RightClickComponent({
                                rowData: record,
                                parentCt: this,
                                menuButtons,
                                formSmsMailList,
                                onEditClick,
                                onDeleteClick,
                                crudFlags
                                
                            })
                        }));
                    })
                )

            )
        );
    }
}
const XPagination = (props) => {
	var pageSize = props.pageSize || 10;
    var {currentPage, totalCount, onChange} = props;
    var totalPages = Math.ceil(totalCount / pageSize);
    var startPage, endPage;
    if (totalPages <= 10) {
        // less than 10 total pages so show all
        startPage = 1;
        endPage = totalPages;
    } else {
        // more than 10 total pages so calculate start and end pages
        if (currentPage <= 6) {
            startPage = 1;
            endPage = 10;
        } else if (currentPage + 4 >= totalPages) {
            startPage = totalPages - 9;
            endPage = totalPages;
        } else {
            startPage = currentPage - 5;
            endPage = currentPage + 4;
        }
    }
    var pages = [...Array(endPage + 1 - startPage).keys()].map(
        i => startPage + i
    );
    let hndlClick = currentPage => event => {
        event.preventDefault();
        event.stopPropagation();
        onChange(currentPage);
    };
    return _(
        Pagination, Object.assign({}, props, {className:"p-0",listClassName: 'd-flex' }),
        _(
            PaginationItem, { disabled: currentPage == 1 },
            _(PaginationLink, {
                previous: true,
                href: "#",
                onClick: hndlClick(currentPage - 1)
            })
        ),
        (pages || []).map((page, index) =>
            _(
                PaginationItem, { active: currentPage === page, key: index, className: 'd-none d-sm-flex' },
                _(PaginationLink, { href: "#", onClick: hndlClick(page) }, page)
            )
        ),
        _('span', { className: 'd-block d-sm-none pr-1' }),
        _(
            PaginationItem, { disabled: currentPage >= totalPages },
            _(PaginationLink, {
                next: true,
                href: "#",
                onClick: hndlClick(currentPage + 1)
            })
        )
    );
};
XPagination.propTypes = {
    currentPage: PropTypes.oneOfType([PropTypes.number, PropTypes.string]),
    pageSize: PropTypes.oneOfType([PropTypes.number, PropTypes.string]),
    totalCount: PropTypes.oneOfType([PropTypes.number, PropTypes.string]),
    onChange: PropTypes.func.isRequired
};
const XItemPerPage = (props) => {
	var { pageSize, onChange, pageSizes } = props;
    let hndlClick = number => event => {
        event.preventDefault();
        event.stopPropagation();
        number != pageSize && onChange && onChange(number);
    };
    return _(
        Pagination, Object.assign({
            "aria-label": getLocMsg("item_per_page"),
            className: "m-0 p-0 p-sm-3"},
            props
        ),
        pageSizes.map((number, index) => {
            return _(
                PaginationItem, {
                    active: number == pageSize || (pageSize == 0 && number == 25) ?
                        true :
                        false,
                    key: number + index
                },
                _(
                    PaginationLink, {
                        href: "#",
                        "aria-label": "number:" + number,
                        onClick: hndlClick(number)
                    },
                    number
                )
            );
        })
    );
};
XItemPerPage.defaultProps = {
    pageSize: 0,
    pageSizes: [5, 10, 25, 100],
    onChange: () => console.warn("XItemPerPage")
};
XItemPerPage.propTypes = {
    onChange: PropTypes.func,
    pageSize: PropTypes.oneOfType([PropTypes.number, PropTypes.string]),
    pageSizes: PropTypes.arrayOf(PropTypes.number)
};
/**
 * @description this component made for render complex ui
 * @example form+grid, grid, form, form+form
 */
class XPage extends React.PureComponent {
    constructor(props) {
        if (iwb.debugConstructor && iwb.debug)
            console.log("XPage.constructor", props);
        super(props);
        var breed = document.getElementById("id-breed");
        if (breed) breed.innerHTML = this.props.grid.name;
        iwb.killGlobalSearch();
        this.state = { activeTab: "x", activeTab2: "x" };
        this.tabs = iwb.tabs[this.props.grid.id] ?
            [...iwb.tabs[this.props.grid.id]] :
            [{ name: "x", icon: "icon-list", title: "Liste", value: props.grid }];
        /**
         * @description a Function to toggle between tabs
         * @param {Event}
         *            event - click event from tab
         */
        this.toggle = event => {
            var activeTab = event.target ? event.target.getAttribute("name") : event;
            if (this.state.activeTab !== activeTab) {
                var { tabs } = this;
                tabs &&
                    tabs.forEach(tempTab => {
                        if (tempTab.name === activeTab) {
                            this.setState({
                                activeTab
                            });
                            return true;
                        }
                    });
            }
            return false;
        };
        this.isActionInTabList = action => {
            var stopToFetch = false;
            this.tabs &&
                this.tabs.forEach(tempTab => {
                    if (tempTab.name === action) {
                        this.toggle(action);
                        stopToFetch = true;
                    }
                });
            return stopToFetch;
        };
        /**
         * @description A function responsible for opening tab getting component
         *              from the server and evaluating it on the page
         * @param {String}
         *            action - ['1-&toffer_id=4'] EditForm satrts 1-* , InsertForm
         *            satrts 2-*
         * @param {String}
         *            url - ['showForm?a=1&_fid=3988&twork_position_id=1']
         * @param {Object}
         *            params - a varible wich holds request body params
         * @param {Object}
         *            callAttributes - [{modal:false}] a variable used to pass
         *            params to a component which comes from the server
         */
        this.openTab = (action, url, params, callAttributes) => {
            if (this.state.activeTab !== action) {
                if (this.isActionInTabList(action)) return;
                iwb.loadingActive(() => {
                    fetch(url, {
                            body: JSON.stringify(params || {}), // must match 'Content-Type'
                            // header
                            cache: "no-cache", // *default, no-cache, reload, force-cache,
                            // only-if-cached
                            credentials: "same-origin", // include, same-origin, *omit
                            headers: {
                                "content-type": "application/json"
                            },
                            method: "POST", // *GET, POST, PUT, DELETE, etc.
                            mode: "cors", // no-cors, cors, *same-origin
                            redirect: "follow", // *manual, follow, error
                            referrer: "no-referrer" // *client, no-referrer
                        })
                        .then(
                            response =>
                            response.status === 200 || response.status === 0 ?
                            response.text() :
                            Promise.reject(
                                new Error(response.text() || response.statusText)
                            )
                        )
                        .then(
                            result => {
                                if (result) {
                                    var f = new Function('callAttributes', 'parentCt', result);
                                    var serverComponent = f(callAttributes || {}, this);
                                    if (serverComponent) {
                                        if (callAttributes && callAttributes.modal) {
                                            // console.log(callAttributes);
                                            iwb.showModal(Object.assign({
                                                body: serverComponent,
                                                size: iwb.defaultModalSıze || "lg",
                                                title: serverComponent.props && serverComponent.props.cfg ?
                                                    serverComponent.props.cfg.name :
                                                    "",
                                                color: callAttributes.modalColor ?
                                                    callAttributes.modalColor :
                                                    "primary",
                                            },callAttributes.modalProps
                                            ));
                                        } else {
                                            var plus = action.substr(0, 1) == "2";
                                            if (this.isActionInTabList(action)) return;
                                            this.tabs.push({
                                                name: action,
                                                icon: plus ? "icon-plus" : "icon-note",
                                                title: " " + getLocMsg(plus ? "new" : "edit"),
                                                value: serverComponent
                                            });
                                            this.setState({
                                                    activeTab: action
                                                },
                                                () => iwb.loadingDeactive()
                                            );
                                        }
                                    }
                                } else {
                                    iwb.loadingDeactive();
                                    toastr.error(getLocMsg("no_result"), " Error");
                                }
                            },
                            error => {
                                iwb.loadingDeactive();
                                toastr.error(error, getLocMsg("connection_error"));
                            }
                        );
                });
            }
        };
        iwb.openTab = this.openTab;
        /**
         * @description A function responsible for closing tab and delating
         *              CurrentTab from the state of Xpage Component this function
         *              will be passed to whenever new tab is opened
         */
        this.closeTab = (event, forceRelaod = false) => {
            if (this.state.activeTab == "x") return;
            this.tabs =
                this.tabs &&
                this.tabs.filter(tempTab => tempTab.name !== this.state.activeTab);
            if (forceRelaod) {
                this.tabs["0"].value.forceRelaod = Math.floor(Math.random() * 1000);
            }
            this.toggle("x");
        };
        iwb.closeTab = this.closeTab;
        /**
         * @description A function is used to open new FormTab
         * @param {string}
         *            url
         */
        this.openForm = (url, callAttributes = {}) => {
            if (url) this.openTab("1-" + Math.random(), url, {}, callAttributes);
            return false;
        };
        iwb.openForm = this.openForm;
    }
    componentWillUnmount() {
        iwb.killGlobalSearch();
        iwb.tabs[this.props.grid.id] = [...this.tabs];
    }
    render() {
        if (iwb.debugRender)
            if (iwb.debug) console.log("XPage.render");
        return _(
            "div", {},
            _(
                Row,
                null,
                _(
                    Col, { className: "mb-4" },
                    _(
                        Nav, { tabs: true, hidden: this.tabs.length == 1 },
                        this.tabs.map(({ name, icon, title }, index) => {
                            return _(
                                NavItem, { key: "NavItem" + index },
                                _(
                                    NavLinkS, {
                                        className: classNames({
                                            active: this.state.activeTab === name
                                        }),
                                        name,
                                        onClick: event => this.toggle(event)
                                    },
                                    _("i", {
                                        className: classNames("mr-1", icon),
                                        name,
                                        title,
                                        onClick: event => this.toggle(event)
                                    }),
                                    title && name != "x" && this.state.activeTab === name && title
                                )
                            );
                        })
                    ),
                    _(
                        TabContent, { activeTab: this.state.activeTab },
                        this.tabs.map(({ name, value }, index) => {
                            return _(
                                TabPane, { key: "TabPane" + index, tabId: name },
                                value.gridId ?
                                _(XMainGrid, Object.assign({
                                    openTab: this.openTab,
                                    closeTab: this.closeTab,
                                },value
                                )) :
                                value
                            );
                        })
                    )
                )
            )
        );
    }
}

class XPage4Card extends React.PureComponent {
        constructor(props) {
            if (iwb.debugConstructor && iwb.debug)
                console.log("XPage4Card.constructor", props);
            super(props);
            document.getElementById("id-breed").innerHTML = this.props.card.name;
            iwb.killGlobalSearch();
            this.state = { activeTab: "x" };
            this.tabs = iwb.tabs[this.props.card.cardId] ?
                [...iwb.tabs[this.props.card.cardId]] :
                [{ name: "x", icon: "icon-list", title: "Liste", value: props.card }];
            /**
             * @description a Function to toggle between tabs
             * @param {Event}
             *            event - click event from tab
             */
            this.toggle = event => {
                var activeTab = event.target ? event.target.getAttribute("name") : event;
                if (this.state.activeTab !== activeTab) {
                    var { tabs } = this;
                    tabs &&
                        tabs.forEach(tempTab => {
                            if (tempTab.name === activeTab) {
                                this.setState({
                                    activeTab
                                });
                                return true;
                            }
                        });
                }
                return false;
            };
            this.isActionInTabList = action => {
                var stopToFetch = false;
                this.tabs &&
                    this.tabs.forEach(tempTab => {
                        if (tempTab.name === action) {
                            this.toggle(action);
                            stopToFetch = true;
                        }
                    });
                return stopToFetch;
            };
            /**
             * @description A function responsible for opening tab getting component
             *              from the server and evaluating it on the page
             * @param {String}
             *            action - ['1-&toffer_id=4'] EditForm satrts 1-* ,
             *            InsertForm satrts 2-*
             * @param {String}
             *            url - ['showForm?a=1&_fid=3988&twork_position_id=1']
             * @param {Object}
             *            params - a varible wich holds request body params
             * @param {Object}
             *            callAttributes - [{modal:false}] a variable used to pass
             *            params to a component which comes from the server
             */
            this.openTab = (action, url, params, callAttributes) => {
                if (this.state.activeTab !== action) {
                    if (this.isActionInTabList(action)) return;
                    fetch(url, {
                            body: JSON.stringify(params || {}), // must match 'Content-Type'
                            // header
                            cache: "no-cache", // *default, no-cache, reload, force-cache,
                            // only-if-cached
                            credentials: "same-origin", // include, same-origin, *omit
                            headers: {
                                "content-type": "application/json"
                            },
                            method: "POST", // *GET, POST, PUT, DELETE, etc.
                            mode: "cors", // no-cors, cors, *same-origin
                            redirect: "follow", // *manual, follow, error
                            referrer: "no-referrer" // *client, no-referrer
                        })
                        .then(
                            response =>
                            response.status === 200 || response.status === 0 ?
                            response.text() :
                            Promise.reject(
                                new Error(response.text() || response.statusText)
                            )
                        )
                        .then(
                            result => {
                                if (result) {
                                    var f = new Function('callAttributes', 'parentCt', result);
                                    var serverComponent = f(callAttributes || {}, this);
                                    if (serverComponent) {
                                        if (callAttributes && callAttributes.modal) {
                                            // console.log(callAttributes);
                                            iwb.showModal(Object.assign({
                                                body: serverComponent,
                                                size: iwb.defaultModalSize || "lg",
                                                title: serverComponent.props && serverComponent.props.cfg ?
                                                    serverComponent.props.cfg.name :
                                                    "",
                                                color: callAttributes.modalColor ?
                                                    callAttributes.modalColor :
                                                    "primary",
                                            }, callAttributes.modalProps
                                            ));
                                        } else {
                                            var plus = action.substr(0, 1) == "2";
                                            if (this.isActionInTabList(action)) return;
                                            this.tabs.push({
                                                name: action,
                                                icon: plus ? "icon-plus" : "icon-note",
                                                title: " " + getLocMsg(plus ? "new" : "edit"),
                                                value: serverComponent
                                            });
                                            this.setState({
                                                activeTab: action
                                            });
                                        }
                                    }
                                } else {
                                    toastr.error("Sonuc Gelmedi", " Error");
                                }
                            },
                            error => {
                                toastr.error(error, "Connection Error");
                            }
                        );
                }
            };
            iwb.openTab = this.openTab;
            /**
             * @description A function responsible for closing tab and delating
             *              CurrentTab from the state of Xpage Component this
             *              function will be passed to whenever new tab is opened
             */
            this.closeTab = (event, forceRelaod = false) => {
                if (this.state.activeTab == "x") return;
                this.tabs =
                    this.tabs &&
                    this.tabs.filter(tempTab => tempTab.name !== this.state.activeTab);
                if (forceRelaod) {
                    this.tabs["0"].value.forceRelaod = Math.floor(Math.random() * 1000);
                }
                this.toggle("x");
            };
            iwb.closeTab = this.closeTab;
            /**
             * @description A function is used to open new FormTab
             * @param {string}
             *            url
             */
            this.openForm = (url, callAttributes = {}) => {
                if (url) this.openTab("1-" + Math.random(), url, {}, callAttributes);
                return false;
            };
            iwb.openForm = this.openForm;
        }
        componentWillUnmount() {
            iwb.killGlobalSearch();
            iwb.tabs[this.props.card.cardId] = [...this.tabs];
        }
        render() {
            if (iwb.debugRender)
                if (iwb.debug) console.log("XPage.render");
            return _(
                "div", {},
                _(
                    Row,
                    null,
                    _(
                        Col, { className: "mb-4" },
                        _(
                            Nav, { tabs: true, hidden: this.tabs.length == 1 },
                            this.tabs.map(({ name, icon, title }, index) => {
                                return _(
                                    NavItem, { key: "NavItem" + index },
                                    _(
                                        NavLinkS, {
                                            className: classNames({
                                                active: this.state.activeTab === name
                                            }),
                                            name,
                                            onClick: event => this.toggle(event)
                                        },
                                        _("i", {
                                            className: icon,
                                            name,
                                            title,
                                            onClick: event => this.toggle(event)
                                        }),
                                        title && name != "x" && this.state.activeTab === name && title
                                    )
                                );
                            })
                        ),
                        _(
                            TabContent, { activeTab: this.state.activeTab },
                            this.tabs.map(({ name, value }, index) => {
                                return _(
                                    TabPane, { key: "TabPane" + index, tabId: name },
                                    value.cardId ?
                                    _(XMainCard, Object.assign({
                                        openTab: this.openTab,
                                        closeTab: this.closeTab,
                                    },value
                                    )) :
                                    value
                                );
                            })
                        )
                    )
                )
            );
        }
    }
    /**
     * @description this component is mostly used for render menu page You can set
     *              ti as a home page
     * @param {String}
     *            props.color - ["primary"] Color class of the card
     * @param {String}
     *            props.color2 - ["#2eadd3"] Color of the icon
     * @param {String}
     *            props.color3 - Fadein color of the card
     * @param {Object}
     *            props.node - MINI MENU data
     * @param {String}
     *            props.node.icon - ["icon-heart"] icon class of the menu
     * @param {String}
     *            props.node.name - ['Teklif/Talep Listesi'] name of the menu
     * @param {String}
     *            props.node.url - ["/mnu_2477/showPage2352"] - URL of the router
     * @param {Boolean}
     *            props.node.visited - Visited?
     */
class XCardMenu extends React.PureComponent {
        render() {
            return _(
                Col, { xs: "12", sm: "6", md: "6", lg: "6", xl: "4" },
                _(
                    Link, { to: this.props.node.url },
                    _(
                        Card, {
                            className: "card-menu text-white bg-" + this.props.color,
                            style: this.props.fadeOut ?
                                { opacity: 0, transform: "scale(.9)" } :
                                this.props.fadeOut === false ?
                                { transform: "scale(1.1)" } :
                                {}
                        },
                        _("i", {
                            className: "big-icon " + (this.props.node.icon || "icon-settings"),
                            style: this.props.color3 ? { color: this.props.color3 } : {}
                        }),
                        _(
                            CardBlock, { className: "pb-0" },
                            this.props.fadeOut === false ?
                            _(
                                "div", {
                                    className: "float-right",
                                    style: {
                                        height: "56px",
                                        width: "56px",
                                        background: "white",
                                        padding: "0px",
                                        borderRadius: "55px"
                                    }
                                },
                                iwb.loaders.puff(56, 56, this.props.color2)
                            ) :
                            _("i", {
                                className: "float-right " + (this.props.node.icon || "icon-settings"),
                                style: {
                                    fontSize: "30px",
                                    background: "white",
                                    padding: "13px",
                                    borderRadius: "55px",
                                    color: this.props.color2
                                }
                            }),
                            _("h1", { className: "mb-0" }, this.props.node.name),
                            _("p", null, this.props.node.name + " ile ilgili işlemler")
                        )
                    )
                )
            );
        }
    }
    /**
     * @description it is used to list opened pages on the main page
     * @param {String}
     *            props.color - ['gray-700'] - color class of the Card
     * @param {Boolean}
     *            props.fadeOut - Card Animation
     * @param {Object}
     *            props.node - MINI MENU data
     * @param {String}
     *            props.node.icon - ["icon-heart"] icon class of the menu
     * @param {String}
     *            props.node.name - ['Teklif/Talep Listesi'] name of the menu
     * @param {String}
     *            props.node.url - ["/mnu_2477/showPage2352"] - URL of the router
     */
class XCardMiniMenu extends React.PureComponent {
        render() {
            let {
                color,
                fadeOut,
                node: { icon, name, url }
            } = this.props;
            return _(
                Col, { xs: "4", sm: "3", md: "2", lg: "2", xl: "1" },
                _(
                    Link, { to: url },
                    _(
                        Card, {
                            className: "card-mini-menu text-white bg-" + color,
                            style: fadeOut ?
                                { opacity: 0, transform: "scale(.9)" } :
                                fadeOut === false ?
                                { transform: "scale(1.1)" } :
                                {}
                        },
                        _(
                            CardBlock, { className: "pb-1", style: { textAlign: "center", padding: "0" } },
                            _("i", {
                                className: icon || "icon-settings",
                                style: { fontSize: "28px", padding: "12px", color: "white" }
                            })
                        )
                    )
                ), !fadeOut && _("h6", { style: { textAlign: "center" } }, name)
            );
        }
    }
    /**
     * @description used to render left menu it gets data from index.htm file
     *              (catche)
     * @param {String}
     *            props.path - [/iwb-home"] path of the current route
     * @param {String}
     *            props.node.icon - ["icon-heart"] icon class of the menu
     * @param {String}
     *            props.node.name - ['Teklif/Talep Listesi'] name of the menu
     * @param {String}
     *            props.node.url - ["/mnu_2477/showPage2352"] - URL of the router
     */
class XMainNav extends React.PureComponent {
        constructor(props) {
            if (iwb.debugConstructor)
                if (iwb.debug) console.log("XMainNav.constructor", props);
            super(props);
            this.onGlobalSearch = inputValue =>
                this.setState({
                    xsearch: inputValue && inputValue.target ? inputValue.target.value : inputValue
                });
            iwb.onGlobalSearch2 = this.onGlobalSearch;
            this.state = { xsearch: "" };
        }
        componentWillUnmount() {
            if (iwb.debug) console.log("XMainNav.componentWillUnmount");
            iwb.killGlobalSearch();
        }
        componentDidUpdate() {
            if (iwb.debug) console.log("XMainNav.componentDidUpdate");
        }
        render() {
            if (iwb.debug) console.log("this.state.xsearch", this.state.xsearch);
            if (this.state.xsearch) {
                var nodes = iwb.nav.findNodes(this.state.xsearch.toLowerCase(), {
                    name: "Home",
                    children: iwb.nav.items
                });
                if (iwb.debug) console.log("nodes", nodes);
                if (!nodes || !nodes.length) return "Bulunamadı :(";
                return _(
                    "div", { className: "animated fadeIn" },
                    _("div", { style: { height: "1.45rem" } }),
                    "Arama Sonuçları",
                    _("hr", { style: { marginTop: "0.4rem" } }),
                    _(
                        Row, { style: { maxWidth: "1300px" } },
                        nodes.map((node, visitedIndex) => {
                            return _(XCardMiniMenu, {
                                color: dgColors3[visitedIndex % dgColors3.length],
                                node
                            });
                        })
                    )
                );
            }
            var path = this.props.path,
                node = this.props.node;
            var visitedList = false,
                saggestedList = false;
            if (path == "/" || path == "/iwb-home") {
                (visitedList = []), (saggestedList = []);
                var visitedIndex = 0,
                    saggestedIndex = 0;
                for (var k in iwb.nav.visitedItems) {
                    var o = iwb.nav.visitedItems[k];
                    visitedList.push(
                        _(XCardMiniMenu, {
                            key: "xcardmini" + Math.random(),
                            color: dgColors3[visitedIndex % dgColors3.length],
                            node: o
                        })
                    );
                    visitedIndex++;
                    if (o.visitCnt > 2) {
                        saggestedList.push(
                            _(XCardMiniMenu, {
                                key: "xcardminivisit" + Math.random(),
                                color: dgColors2[saggestedIndex % dgColors2.length],
                                node: o
                            })
                        );
                        saggestedIndex++;
                    }
                }
                if (visitedIndex == 0) visitedList = false;
                else {
                    visitedList = [
                        _("div", { key: "a1", style: { height: "1.5rem" } }),
                        "Açık Ekranlar",
                        _("hr", { key: "a2", style: { marginTop: "0.4rem" } }),
                        _(Row, { key: "a3", style: { maxWidth: "1300px" } }, visitedList)
                    ];
                    if (saggestedIndex > 0) {
                        if (saggestedList.length > 4) {
                            saggestedList.splice(4, 1000);
                        }
                        visitedList.push(
                            _("div", { style: { height: "1.5rem" } }),
                            "iWB Öneriler",
                            _("hr", { style: { marginTop: "0.4rem" } }),
                            _(Row, { style: { maxWidth: "1300px" } }, saggestedList)
                        );
                    }
                }
            }

            return _(
                "div", { className: "animated fadeIn" },
                _("div", { style: { height: "1.45rem" } }),
                _(
                    Row, {
                        style: { maxWidth: "1300px" }
                    },
                    node.children.map((tempNode, index) =>
                        !iwb.mainMainSmall ? _(XCardMenu, {
                            key: index,
                            node: tempNode,
                            color: dgColors2[index % dgColors2.length],
                            color3: dBGColors2[index % dBGColors2.length],
                            color2: detailSpinnerColors2[index % detailSpinnerColors2.length]
                        }) :
                        _(XCardMiniMenu, {
                            key: index,
                            color: dgColors3[index % dgColors3.length],
                            node: tempNode
                        })
                    )
                ),
                visitedList
            );
        }
    }
    /**
     * @description it renders main part of the application it contains XPage
     *              component of XCardMenu role : component like a container
     * @param {Object}
     *            props.history - history object from react router
     * @param {Object}
     *            props.location - current location object from react router
     * @param {Object}
     *            props.match - same with location object from react router
     */
class XMainPanel extends React.PureComponent {
        constructor(props) {
            if (iwb.debugConstructor) console.log("XMainPanel.constructor", props);
            super(props);
            this.state = { templateID: -1 };
            /**
             * @description A function to load page from the server
             */
            this.loadPage = () => {
                var templateID = this.templateID;
                if (!iwb["t-" + templateID]) {
                    //  if(iwb.loadingPageMask && iwb.loadingActivate)iwb.loadingActivate();
                    fetch("showPage?_tid=" + templateID, {
                            cache: "no-cache", // *default, no-cache, reload, force-cache,
                            // only-if-cached
                            credentials: "same-origin", // include, same-origin, *omit
                            headers: {
                                "content-type": "application/json"
                            },
                            method: "POST", // *GET, POST, PUT, DELETE, etc.
                            mode: "cors", // no-cors, cors, *same-origin
                            redirect: "follow", // *manual, follow, error
                            referrer: "no-referrer" // *client, no-referrer
                        })
                        .then(
                            response => {
                                //if(iwb.loadingPageMask && iwb.loadingDectivate)iwb.loadingDeactivate();
                                return response.status === 200 || response.status === 0 ?
                                    response.text() :
                                    Promise.reject(new Error(response.statusText))
                            })
                        .then(
                            result => {
                                if (result) {
                                    var f = new Function('callAttributes', 'parentCt', result);
                                    var serverComponent = f(false, this);
                                    if (serverComponent) {
                                        serverComponent = _(
                                            React.Suspense, { fallback: _(XLoading, null) },
                                            _(
                                                "div", { className: "animated fadeIn", id: "page" + templateID },
                                                serverComponent
                                            )
                                        );
                                        iwb["t-" + templateID] = serverComponent;
                                        this.setState({ templateID });
                                        iwb.nav.visitItem(this.props.match.path);
                                    } else {
                                    	serverComponent = _(
                                                React.Suspense, { fallback: _(XLoading, null) },
                                                _(
                                                    "div", { className: "animated fadeIn", id: "page" + templateID },
                                                    'Error'
                                                )
                                            );
                                            iwb["t-" + templateID] = 'Error';
                                            this.setState({ templateID });
                                            iwb.nav.visitItem(this.props.match.path);
                                    }
                                } else {
                                    toastr.error("No Data", " Error");
                                }
                            },
                            error => {
                                toastr.error(error, "Connection Error");
                            }
                        ).catch( error=>{
                            toastr.error(error, "Connection Error");
                        	
                        });
                } else if (templateID != this.state.templateID)
                    this.setState({ templateID });
            };
        }
        componentDidMount() {
            if (iwb.debug)
                console.log("XMainPanel.componentDidMount", this.props.match.path);
            if (!this.loading) this.loadPage();
        }
        componentDidUpdate() {
            if (iwb.debug)
                console.log("XMainPanel.componentDidUpdate", this.props.match.path);
            if (!this.loading) this.loadPage();
        }
        componentDidCatch() {
            if (iwb.debug) console.log("XMainPanel.componentDidCatch", this);
        }
        componentWillUnmount() {
            if (iwb.debug)
                console.log("XMainPanel.componentWillUnmount", this.props.match.path);
        }
        render() {
            var { path } = this.props.match;
            var children = { name: "Home", children: iwb.nav.items };
            var node =
                path == "/" || path == "/iwb-home" ?
                children :
                iwb.nav.findNode(path, children);
            if (iwb.debug) console.log("XMainPanel:render:", path, node);
            if (node) {
                var showPageIndex = path.indexOf("showPage");
                if (showPageIndex > -1) {
                    var templateID = 1 * path.substr(showPageIndex + "showPage".length);
                    this.templateID = templateID;
                    if (this.templateID != this.state.templateID) {
                        if (this.loading) {
                            var ll = this.loading;
                            this.loading = false;
                            return _(
                                "div", { className: "animated fadeIn" },
                                _("div", { style: { height: "1.45rem" } }),
                                _(
                                    Row, { style: { maxWidth: "1300px" } },
                                    ll.children.map((menuitem, index) => {
                                        return !iwb.mainMainSmall ? _(XCardMenu, {
                                                key: index,
                                                node: menuitem,
                                                fadeOut: menuitem.url != node.url,
                                                color: dgColors2[index % dgColors2.length],
                                                color3: dBGColors2[index % dBGColors2.length],
                                                color2: detailSpinnerColors2[index % detailSpinnerColors2.length]
                                            }) :
                                            _(XCardMiniMenu, {
                                                key: index,
                                                fadeOut: menuitem.url != node.url,
                                                color: dgColors3[index % dgColors3.length],
                                                node: menuitem
                                            });
                                    })
                                )
                            );
                        }
                        return _(XLoading, null);
                    }
                    var visitedItems = iwb.nav.visitedItems[path];
                    if (visitedItems) visitedItems.visitCnt++;
                    return iwb["t-" + templateID];
                } else {
                    var pageName = document.getElementById("id-breed");
                    if (pageName) {
                        pageName.innerHTML = node.name || "Home";
                    }
                    this.loading = node;
                    return _(iwb.XMainNav || XMainNav, { path, node });
                }
            } else {
                this.loading = false;
                return "ERROR! Wrong Page";
            }
        }
    }
    /**
     * @description Loading Component
     */
class XLoading extends React.Component {
    render() {
        return _(
            "span", { style: { position: "fixed", left: "48%", top: "45%" } },
            iwb.loading
        );
    }
}
class XLoadingSpinner extends React.Component {
        constructor(props) {
            super(props);
            this.state = {
                loading: false
            };
            this.loadingActive = callback => {
                this.setState({
                        loading: true
                    },
                    () => {
                        callback && callback();
                    }
                );
            };
            iwb.loadingActive = this.loadingActive;
            this.loadingDeactive = () => {
                this.setState({
                    loading: false
                });
            };
            iwb.loadingDeactive = this.loadingDeactive;
        }
        render() {
            return (
                this.state.loading &&
                _(
                    "div", {
                        style: {
                            opacity: "0.1"
                        },
                        className: "modal-backdrop show"
                    },
                    _(
                        "span", {
                            style: {
                                position: "fixed",
                                left: "48%",
                                top: "45%"
                            }
                        },
                        iwb.loading
                    )
                )
            );
        }
    }
    /**
     * @description All the Forms will extend from this component so all the props
     *              will come from the server side
     */
class XForm extends React.Component {
        constructor(props) {
            super(props);
            // methods
            /**
             * sets the state with value of input
             *
             * @param {event}
             *            param0
             */
            this.onChange = ({ target }) => {
                var { values } = this.state;
                if (target) {
                	var oldVal = values[target.name];
                    values[target.name] =
                        target.type == "checkbox" ? target.checked : target.value;
                    this.setState({ values });
                    if(target._onChange)target._onChange(values[target.name], oldVal, values);
                }
            };
            /**
             * file uploader function
             */
            this.onFileChange = () => (name, result, context) => {
                var values = this.state.values;
                var errors = this.state.errors;
                if (result.success) {
                    values[name] = result.fileId;
                    errors[name] = undefined;
                } else {
                    errors[name] = result.error;
                }
                this.setState({
                    errors,
                    values
                });
            };
            /**
             * sets state for combo change else sets oprions of it after the request
             *
             * @param {String}
             *            inputName
             */
            this.onComboChange = inputName => selectedOption => {
                var { values } = this.state;
                var selectedOptionId = selectedOption && selectedOption.id;
                values[inputName] = selectedOptionId;
                var triggers = this.triggerz4ComboRemotes;
                // used for remote @depricated
                if (triggers[inputName]) {
                    triggers[inputName].map(trigger => {
                        var nv = trigger.f(selectedOptionId, null, values);
                        var { options } = this.state;
                        if (nv) {
                            iwb.request({
                                url: "ajaxQueryData?" + iwb.JSON2URI(nv) + ".r=" + Math.random(),
                                successCallback: ({ data }) => {
                                    options[trigger.n] = data;
                                    this.setState({ options });
                                }
                            });
                        } else {
                            options[trigger.n] = [];
                            this.setState({ options });
                        }
                    });
                }
                this.setState({ values });
//                if(target._onChange)target._onChange(values[target.name], oldVal, values);

            };

            /**
             * sets state when low combo is entered
             *
             * @param {String}
             *            inputName
             */
            this.onLovComboChange = inputName => selectedOptions => {
                var { values } = this.state;
                var selectedOptionIds = [];
                if (selectedOptions) {
                    selectedOptions.map(selectedOption => {
                    	selectedOptionIds.push(selectedOption.id);
                    });
                }
                values[inputName] = selectedOptionIds.join(",");
                this.setState({ values });
            };

            this.onCheckboxGroupChange = inputName => actionOption => {
                var { values } = this.state;
                var selectedOptionIds = values[inputName] ? values[inputName].split(','):[];
                if (actionOption.checked) {//add
                	selectedOptionIds.push(actionOption.id);
                } else { //remove
                	var ar = []
                	selectedOptionIds.map(o=>{
                		if(o!=actionOption.id)ar.push(o);
                	});
                	selectedOptionIds = ar;
                	
                }
                values[inputName] = selectedOptionIds.join(",");
                this.setState({ values });
            };
            /**
             * sets state when number entered
             *
             * @param {String}
             *            dsc
             */
            this.onNumberChange = inputName => inputEvent => {
                var { values } = this.state;
                var inputValue = inputEvent && inputEvent.value;
                values[inputName] = inputValue;
                this.setState({ values });
            };
            /**
             * sets state when html entered
             */
            this.onHtmlChange = inputName => value => {
                var { values } = this.state;
                values[inputName] = value;
                this.setState({ values });
            };
            /** acts very simmilar */
            this.onTreeSelectChange = this.onHtmlChange;
            /**
             * sends post to the server
             *
             * @param {Object} cfg
             */
            this.submit = cfg => {
                var baseValues = iwb.formBaseValues(cfg.id);
                var values = Object.assign({}, baseValues, this.state.values );
                if (this.componentWillPost) {
                    /**
                     * componentWillPostResult = true || fase || {field_name : 'custom
                     * value'}
                     */
                    var componentWillPostResult = this.componentWillPost(values, cfg || {});
                    if (!componentWillPostResult){
                    	iwb.loadingDeactive();
                    	return false;
                    }
                    values = Object.assign({}, values, componentWillPostResult );
                }
                var requestConfig={
                    url: this.url +
                        "?" +
                        iwb.JSON2URI(this.params) +
                        "_renderer=react16&.r=" +
                        Math.random(),
                    params: values,
                    self: this,
                    errorCallback: json => {
                        iwb.loadingDeactive();
                        var errors = {};
                        if (json.errorType)
                            switch (json.errorType) {
                            	case	"confirm":
                            		yesNoDialog({
                                        text: json.error,
                                        callback: success => {
                                            if (success) {
                                            	requestConfig.params['_confirmId_'+json.objectId]=1;
                                    			iwb.request(requestConfig);
                                            }
                                        }
                                    });
                            		break;
                                case "validation":
                                	var errMsg = getLocMsg("validation_errors");
                                    if (json.errors && json.errors.length) {
                                        json.errors.map(oneError => {
                                            errors[oneError.id] = oneError.msg;
                                            errMsg+='<li>'+(oneError.dsc||oneError.id)+ ': ' + oneError.msg+'</li>';
                                        });
                                        
                                    }
                                    toastr.error(errMsg,{ timeOut: 7000 });
                                    	
                                    if (json.error) {
                                        iwb.showModal({
                                            title: "ERROR",
                                            footer: false,
                                            color: "danger",
                                            size: "sm",
                                            body: json.error
                                        });
                                    }
                                    break;
                                case "framework":
                                case "security":
                                case "sql":
                                case "rhino":
                                    if (json.error) {
                                        iwb.showModal({
                                            title: json.error,
                                            footer: false,
                                            color: "danger",
                                            size: "lg",
                                            body: _(
                                                Media, {
                                                    body: true
                                                },
                                                json.objectType &&
                                                _(
                                                    Media, {
                                                        heading: true
                                                    },
                                                    json.objectType
                                                ),

                                                _(
                                                    ListGroup, {},
                                                    json.icodebetter &&
                                                    json.icodebetter.map((item, index) => {
                                                        return _(
                                                            ListGroupItem, {},
                                                            _(
                                                                ListGroupItemHeading, {},
                                                                item.errorType,
                                                                item &&
                                                                _(
                                                                    Button, {
                                                                        className: "float-right btn btn-xs",
                                                                        color: "info",
                                                                        onClick: e => {
                                                                            e.preventDefault();
                                                                            iwb.copyToClipboard(item);
                                                                        }
                                                                    },
                                                                    _(
                                                                        "i", {
                                                                            className: "icon-docs"
                                                                        },
                                                                        ""
                                                                    )
                                                                ),
                                                                item &&
                                                                _(
                                                                    Button, {
                                                                        className: "float-right btn btn-xs",
                                                                        color: "primary",
                                                                        onClick: e => {
                                                                            e.preventDefault();
                                                                            iwb.log(item);
                                                                            toastr.success(
                                                                                "Use CTR + SHIFT + I to see the log content!",
                                                                                "Console Log", { timeOut: 3000 }
                                                                            );
                                                                        }
                                                                    },
                                                                    _(
                                                                        "i", {
                                                                            className: "icon-target"
                                                                        },
                                                                        ""
                                                                    )
                                                                )
                                                            ),
                                                            _(
                                                                ListGroupItemText, {},
                                                                item &&
                                                                _(
                                                                    "pre", {},
                                                                    window.JSON.stringify(item, null, 2)
                                                                )
                                                            )
                                                        );
                                                    })
                                                )
                                            )
                                        });
                                    }
                                    break;
                                default:
                                    alert(json.errorType);
                            }
                        else alert(json);
                        this.setState({ errors });
                        return false;
                    },
                    successCallback: (json, xcfg) => {
                        iwb.loadingDeactive();
                        this.setState({errors:{}});
                        if (cfg.callback) cfg.callback(json, xcfg);
                    }
                }
                iwb.request(requestConfig);
            };
            /**
             * used to make form active tab and visible on the page
             *
             * @param {object}
             *            tab
             */
            this.toggleTab = tab => {
                if (this.state.activeTab !== tab) {
                    this.setState({ activeTab: tab });
                }
            };
            /**
             * returns form data from state
             */
            this.getValues = () => Object.assign({}, this.state.values );
            /**
             * used for date inputs
             *
             * @param {String} inputName
             * @param {Boolean} isItDTTM
             */
            this.onDateChange = (inputName, isItDTTM) => selectedDate => {
                var values = this.state.values;
                var dateValue = selectedDate && selectedDate._d;
                values[inputName] = isItDTTM ?
                    fmtDateTime(dateValue) :
                    fmtShortDate(dateValue);
                this.setState({ values });
            };
            this.onTimeChange = (inputName) => momentObject => {
            	var time = momentObject;
            	if(typeof(momentObject) !== "string"){
            		time = momentObject.format("hh:mm a");
            	}
            	var values = this.state.values;
            	values[inputName] = time;
            	this.setState({values});
            }
            
        }
        componentDidMount() {
            var triggers = this.triggerz4ComboRemotes;
            var { values } = this.state;
            for (var trigger in triggers) {
                if (values[trigger]) {
                    triggers[trigger].map(zzz => {
                        var nv = zzz.f(values[trigger], null, values);
                        if (nv)
                            iwb.request({
                                url: "ajaxQueryData?" + iwb.JSON2URI(nv) + ".r=" + Math.random(),
                                successCallback: ({ data }) => {
                                    var { options } = this.state;
                                    options[zzz.n] = data;
                                    this.setState({ options });
                                }
                            });
                    });
                }
            }
        }
        componentWillUnmount() {
            iwb.forms[this._id] = Object.assign({}, this.state );
        }
    }
    /**
     * File Input Component to upload file into system
     */
class FileInput extends React.Component {
    constructor(props) {
        super(props);
        this.state = {
            canUpload: false,
            dragOver: false,
            file: null,
            fileUrl: props.fileId ?
                "dl/" +
                props.fileName +
                "?_fai=" +
                props.fileId +
                "&.r=" +
                Math.random() :
                null,
            fileName: props.fileName || null
        };
        this.onDrop = this.onDrop.bind(this);
        this.dragenter = this.dragenter.bind(this);
        this.dragleave = this.dragleave.bind(this);
        this.dragover = this.dragover.bind(this);
        this.onclick = this.onclick.bind(this);
        this.onchange = this.onchange.bind(this);
        this.uploadFile = this.uploadFile.bind(this);
        /** */
        this.downladLink = url => e => {
            e.preventDefault();
            e.stopPropagation();

            let link = document.createElement("a");
            link.href = url; link.target="_blank";
            document.body.appendChild(link);
            link.click();
            document.body.removeChild(link);
        };
    }
    componentWillMount() {}
        /** function to click input ref click */
    onclick(event) {
            event.preventDefault();
            event.stopPropagation();
            this.inpuRef.click();
        }
        /** used to disable opening file on new tab */
    dragover(event) {
            event.preventDefault();
            event.stopPropagation();
        }
        /** used with css */
    dragleave(event) {
            event.preventDefault();
            event.stopPropagation();
            this.setState({
                dragOver: false
            });
        }
        /** when the file over drag area */
    dragenter(event) {
            event.preventDefault();
            event.stopPropagation();
            this.setState({
                dragOver: true
            });
        }
        /** when the file dproped over drop area */
    onDrop(event) {
            event.preventDefault();
            event.stopPropagation();
            this.setState({
                    canUpload: true,
                    dragOver: false,
                    file: event.dataTransfer.files[0]
                },
                () => {
                    this.uploadFile();
                }
            );
        }
        /** when the file dproped over drop area */
    onchange(event) {
            event.preventDefault();
            event.stopPropagation();
            this.setState({
                    canUpload: true,
                    dragOver: false,
                    file: event.target.files[0]
                },
                () => {
                    this.uploadFile();
                }
            );
        }
        /** uploader function */
    uploadFile() {
        if (!this.state.file) {
            return;
        }
        let formData = new FormData();
        formData.append(
            "table_pk",
            this.props.cfg.tmpId ? this.props.cfg.tmpId : json2pk(this.props.cfg.pk)
        );
        formData.append("table_id", this.props.cfg.crudTableId);
        formData.append("file", this.state.file);
        formData.append("profilePictureFlag", this.props.profilePictureFlag || 0);
        fetch("upload.form", {
                method: "POST",
                body: formData,
                cache: "no-cache",
                credentials: "same-origin",
                mode: "cors",
                redirect: "follow",
                referrer: "no-referrer"
            })
            .then(
                response =>
                response.status === 200 || response.status === 0 ?
                response.json() :
                Promise.reject(new Error(response.text() || response.statusText))
            )
            .then(
                result => {
                    if (result.success) {
                        toastr.success(
                            getLocMsg("file_sucessfully_uploaded"),
                            getLocMsg("success"), {
                                timeOut: 3000
                            }
                        );
                        this.setState({
                            canUpload: false,
                            file: null,
                            fileName: result.fileName,
                            fileUrl: result.fileUrl
                        });
                    } else {
                        if (result.error) {
                            toastr.error(result.error, result.errorType);
                        }
                    }
                    this.props.onFileChange &&
                        this.props.onFileChange(this.props.name, result, this);
                    return;
                },
                error => {
                    toastr.error(error, getLocMsg("error"));
                }
            );
    }
    render() {
        let defaultStyle = {
            height: "100%",
            width: "100%",
            position: "absolute",
            top: "0",
            left: "0"
        };
        return _(
            React.Fragment, {},
            _(
                "div",
                null,
                // this.state.file ? getLocMsg(this.state.file.name) : getLocMsg('File Upload'),
                _("input", {
                    className: "d-none",
                    type: "file",
                    onChange: this.onchange,
                    ref: input => (this.inpuRef = input)
                }),
                this.props.extraButtons && this.props.extraButtons
            ),
            _(
                "div", {},
                _(
                    "div", {
                        className: "mx-auto",
                        style: {
                            height: "200px",
                            width: "200px",
                            position: "relative",
                            border: this.state.dragOver ?
                                "3px dashed #20a8d8" :
                                "3px dashed #a4b7c1"
                        }
                    },
                    _("div", {
                        style: Object.assign({}, defaultStyle,
                            {zIndex: "10",
                            background: "gray",
                            cursor: "pointer",
                            opacity: this.state.canUpload ? "0" : "0.5"
                        }),
                        className: "rounded",
                        onDrop: this.onDrop,
                        onDragEnter: this.dragenter,
                        onDragLeave: this.dragleave,
                        onDragOver: this.dragover,
                        onClick: this.onclick
                    }),
                    _(
                        "div", {
                            style: Object.assign({}, defaultStyle,
                                {display: "flex"
                            })
                        },
                        _(XPreviewFile, {
                            file: this.state.file
                        })
                    )
                ),
                _("div", {
                    className: "clearfix"
                }),
                _(
                    ListGroup, {},
                    this.state.fileUrl &&
                    _(
                        ListGroupItem,
                        null,
                        _(
                            "a", {
                                onClick: this.downladLink(this.state.fileUrl),
                                href: "#"
                            },
                            this.state.fileName
                        )
                    )
                )
            )
        );
    }
}



function fmtDecimal(value, digit,precision) {
    if (!value) return '0';
    if (!digit) digit = 2;
    var result = Math.round(value * Math.pow(10, digit)) / Math.pow(10, digit);
    result = Number.parseFloat(result).toFixed(precision) + '';
    var s = 1 * result < 0 ? 1 : 0;
    var x = result.split('.');
    var x1 = x[0],
        x2 = x[1];
    for (var i = x1.length - 3; i > s; i -= 3) x1 = x1.substr(0, i) + ('.') + x1.substr(i);
    if (x2 && x2 > 0) return x1 + (',') + x2;
    return x1;
}


iwb.apexCharts = {}
iwb.graph = function(dg, gid, callback) {
    if (!dg.groupBy) return;
    var newStat = 1 * dg.funcTip ? dg.funcFields : "";
    var params = {};
    if (newStat) params._ffids = newStat;
    if (1 * dg.graphTip >= 5) params._sfid = dg.stackedFieldId;
    var series = [],
        labels = [],
        lookUps = [],
        chart = null;
    var xid = gid;
    var el = document.getElementById(gid);
    if (!el) return;
    iwb.request({
        url:
            (dg.query ? "ajaxQueryData4Stat?_gid=" : "ajaxQueryData4StatTree?_gid=") +
            dg.gridId +
            "&_stat=" +
            dg.funcTip +
            "&_qfid=" +
            dg.groupBy +
            "&_dtt=" +
            dg.dtTip,
        params: Object.assign(params, dg.queryParams),
        successCallback: function(j) {
            var d = j.data;
            if (!d || !d.length) return;
            switch (1 * dg.graphTip) {
                case 6: // stacked area
                case 5: // stacked column
                    var l = j.lookUp;
                    for (var k in l) lookUps.push(k);
                    if (!lookUps.length) return;
                    d.map((z) => {
                        var data = [];
                        lookUps.map((y) => data.push(1 * (z['xres_' + y] || 0)));
                        series.push({ name: z.dsc, data: data });
                    });
                    lookUps.map((y) => labels.push(l[y] || '-'));

                    options = {
                        chart: {
                            id: 'apex-' + gid,
                            //	                    height: 80*d.length+40,
                            type: 'bar',
                            stacked: true,
                            toolbar: { show: false }
                        },
                        plotOptions: {
                            //	                    bar: {horizontal: true},

                        },
                        series: series,
                        //title: {text: dg.name},
                        xaxis: {
                            categories: labels,
                        },
                        yaxis: { labels: { show: !!dg.legend }, axisTicks: { color: '#777' } },
                    }
                    break;
                case 3: // pie
                    d.map((z) => {
                        series.push(1 * z.xres);
                        labels.push(z.dsc || '-');
                    });
                    var options = {
                        chart: { id: 'apex-' + gid, type: 'donut', toolbar: { show: false } },
                        series: series,
                        labels: labels,
                        legend: dg.legend ? { position: dg.legend===true?'bottom':dg.legend } : { show: false },
                        dataLabels: dg.legend ? {} : { formatter: function(val, opts) { return labels[opts.seriesIndex] + ' - ' + fmtDecimal(val); } }
                    }

                    break;
                case 1: // column
                case 2: // line
                    var colCount = newStat.split(',').length;
                    for (var qi = 0; qi < colCount; qi++) {
                        series.push({ name: j.lookUps ? j.lookUps[qi] : ('Count'), data: [] })
                    }
                    d.map((z) => {
                        for (var qi = 0; qi < colCount; qi++) {
                            series[qi].data.push(1 * z[qi ? 'xres' + (qi + 1) : 'xres']);
                        }
                        labels.push(z.dsc);
                    });

                    options = {
                        chart: {
                            id: 'apex-' + gid,
                            //	                    height:document.getElementById()50*d.length+30,
                            type: 1 * dg.graphTip == 1 ? 'bar' : 'spline',
                            toolbar: { show: false }
                        },
                        stroke: 1 * dg.graphTip == 1 ? {} : {
                            curve: 'smooth'
                        },
                        series: series,
                        xaxis: {
                            categories: labels,
                        },
                        yaxis: { labels: { show: !!dg.legend } },
                    }
                    break;
            }

            if (options) {
                options.theme = {
                    //		            mode: 'dark',
                    palette: iwb.graphPalette || 'palette6',
                };
                options.chart.height = el.offsetHeight && el.offsetHeight > 50 ? el.offsetHeight - 20 : el.offsetWidth / 2;
                if (iwb.apexCharts[xid]) iwb.apexCharts[xid].destroy();
                if (callback) options.chart.events = {
                    dataPointSelection: function(event, chartContext, config) {
                        if (config.selectedDataPoints && config.selectedDataPoints && config.selectedDataPoints.length) {
                            var yx = config.selectedDataPoints[0];
                            callback(yx.length ? d[yx[0]].id : false);
                        }
                    }
                }
                var chart = new ApexCharts(
                    el,
                    options
                );
                iwb.apexCharts[xid] = chart;
                chart.render();
            }

        }
    });
}

iwb.radialBar = function(qid, gid, params){
	iwb.request({
        url: 'ajaxQueryData?_qid=' + qid + '&.r=' + Math.random(),
        params: params||{},
        successCallback: (result2) => {
            if (!result2.data.length) return;
            var j = result2.data[0];
            var options = {
            chart: {
//                height: 300,
                type: 'radialBar',
            },
            plotOptions: {
                radialBar: {
                    startAngle: -135,
                    endAngle: 225,
                    hollow: {
                        margin: 0,
                        size: '70%',
                        background: '#fff',
                        image: undefined,
                        imageOffsetX: 0,
                        imageOffsetY: 0,
                        position: 'front',
                        dropShadow: {
                            enabled: true,
                            top: 0,
                            left: 0,
                            blur: 4,
                            opacity: 0.24
                        }
                    },
                    dataLabels: {
                        showOn: 'always',
                        name: {
                            offsetY: -10,
                            show: true,
                            color: '#888',
                            fontSize: '17px'
                        },
                        value: {
                            formatter: function(val) {
                                return j.val;
                            },
                            color: '#111',
                            fontSize: '36px',
                            show: true,
                        }
                    }
                }
            },
            series: [100 * j.xval],
            stroke: {
                lineCap: 'round'
            },
            labels: [j.title],

        }

        var xid = gid;
        if (iwb.charts[xid]) iwb.charts[xid].destroy();
        var chart = new ApexCharts(
            document.getElementById(xid),
            options
        );
        iwb.charts[xid] = chart;

        chart.render();
        }
	});
}
iwb.graphQuery = function(dg, gid, params, callback) {
    var series = [],
        labels = [],
        lookUps = [],
        chart = null;
    var xid = gid;
    var el = document.getElementById(gid);
    if (!el) return;
    iwb.request({
        url:
            "ajaxQueryData?_qid=" +dg.queryId,
        params: params||{},
        successCallback: function(j) {
            var d = j.data;
            if (!d || !d.length) return;
            switch (1 * dg.graphTip) {
                case 6: // stacked area
                case 5: // stacked column
                    var colCount = dg.fields.length;
                    for (var qi = 1; qi < colCount; qi++) {
                        series.push({ name: dg.fields[qi].name, data: [] })
                    }
                    d.map((z) => {
                        for (var qi = 1; qi < colCount; qi++) {
                            series[qi-1].data.push(1 * z[dg.fields[qi].id]);
                        }
                        labels.push(z[(dg.fields[0].id+'_qw_')]||z[dg.fields[0].id]|| '-');
                    });
                    
                    options = {
                        chart: {
                            id: 'apex-' + gid,
                            //	                    height: 80*d.length+40,
                            type: dg.graphTip==5?'bar':'area',
                            stacked: true,
                            toolbar: { show: false }
                        },
                        plotOptions: {
                            //	                    bar: {horizontal: true},

                        },
                        series: series,
                        stroke:dg.graphTip==5?{}:{curve:'smooth'},
                        //title: {text: dg.name},
                        xaxis: {
                            categories: labels,
                        },
                        yaxis: { labels: { show: !!dg.legend }, axisTicks: { color: '#777' } },
                    }
                    break;
                case 3: // pie
                    d.map((z) => {
                        series.push(1 * z[dg.fields[1].id]);
                        labels.push(z[(dg.fields[0].id+'_qw_')]||z[dg.fields[0].id]|| '-');
                    });
                    var options = {
                        chart: { id: 'apex-' + gid, type: 'donut', toolbar: { show: false } },
                        series: series,
                        labels: labels,
                        legend: dg.legend ? { position: dg.legend===true?'bottom':dg.legend } : { show: false },
                        dataLabels: dg.legend ? {} : { formatter: function(val, opts) { return labels[opts.seriesIndex] + ' - ' + fmtDecimal(val); } }
                    }

                    break;
                case 1: // column
                case 2: // line
                    var colCount = dg.fields.length;
                    for (var qi = 1; qi < colCount; qi++) {
                        series.push({ name: dg.fields[qi].name, data: [] })
                    }
                    d.map((z) => {
                        for (var qi = 1; qi < colCount; qi++) {
                            series[qi-1].data.push(1 * z[dg.fields[qi].id]);
                        }
                        labels.push(z[(dg.fields[0].id+'_qw_')]||z[dg.fields[0].id]|| '-');
                    });

                    options = {
                        chart: {
                            id: 'apex-' + gid,
                            //	                    height:document.getElementById()50*d.length+30,
                            type: 1 * dg.graphTip == 1 ? 'bar' : 'line',
                            toolbar: { show: false }
                        },
                        stroke: 1 * dg.graphTip == 1 ? {} : {
                            curve: 'smooth'
                        },
                        series: series,
                        xaxis: {
                            categories: labels,
                        },
                        yaxis: { labels: { show: !!dg.legend } },
                    }
                    break;
            }

            if (options) {
                options.theme = {
                    //		            mode: 'dark',
                    palette: iwb.graphPalette || 'palette6',
                };
                options.chart.height = el.offsetHeight && el.offsetHeight > 50 ? el.offsetHeight - 20 : el.offsetWidth / 2;
                if (iwb.apexCharts[xid]) iwb.apexCharts[xid].destroy();
                if (callback) options.chart.events = {
                    dataPointSelection: function(event, chartContext, config) {
                        if (config.selectedDataPoints && config.selectedDataPoints && config.selectedDataPoints.length) {
                            var yx = config.selectedDataPoints[0];
                            callback(yx.length ? d[yx[0]].id : false);
                        }
                    }
                }
                var chart = new ApexCharts(
                    el,
                    options
                );
                iwb.apexCharts[xid] = chart;
                chart.render();
            }

        }
    });
}

class XCardList  extends React.Component {
    constructor(props) {
        super(props);
        this.state = {rows: [], loading: false};
        if(props.setCmp)props.setCmp(this);
        this.loadData = (force, params) => {
            if(force)this.setState({ rows: [], loading: true });
            iwb.request({
                url: this.props._url,
                self: this,
                params: params||{},
                successCallback: (result, cfg) => {
                	if(this.props.onLoad && this.props.onLoad(result, cfg)===false)return;
                    cfg.self.setState({
                        rows: result.data,
                        totalCount: result.total_count,
                        loading: false
                    });
                },
                errorCallback: (error, cfg) => {
                    cfg.self.setState({
                        rows: [],
                        totalCount: 0,
                        loading: false
                    });
                }
            });
        }
        if(props.registerLoad)props.registerLoad(this.loadData);
    }
    componentDidMount() {
        this.loadData();
    }
    componentDidUpdate() {
    }
    render(){
    	var rows = this.state.rows, fnc=this.props.render; 
    
    	return _('div', {}, rows.map(o => fnc(o)))
    }
}


class XPortletItem extends React.PureComponent {
    constructor(props) {
        super(props);
        this.state = {counter:0};

        this.reloadItem = (force, params) => {
        	this.setState({counter:this.state.counter+1});
        	if(this.reloadFnc)this.reloadFnc(!0, params);
        	else if(this.props.graph){
                var dg = this.props.graph;
                var gid = "idG" + dg.graphId;
                iwb.graph(dg, gid);
        	} else if(this.props.gquery){
                var dg = this.props.gquery;
                var gid = "idGQ" + dg.queryId;
                iwb.graphQuery(Object.assign({}, dg, this.props.props||{}), gid, params||{});
        	} else if(this.props.gauge){
                var dg = this.props.gauge;
                var gid = "idGA" + dg;
                iwb.radialBar(dg, gid, params||{});
        	} 
        }
        if(props.registerLoad)props.registerLoad(this.reloadItem);
    }
    
    componentDidMount() {
    	this.reloadItem();
    }
    
    render(){
    	var o = this.props;
        var name = o.graph || o.grid || o.card || o.query || o.gquery || o.component || o.page || o.gauge;
        if (!name) return false;//_("div", null, "not portlet");
        
        if (o.query) {//badge
            var q = o.query.data;
            if (!q || !q.length) return _("div", null, "not data");
            q = q[0];
            return _(
                Card, {
                    className: "card-portlet text-white bg-" + (o.props.color || "primary")
                },
                _("i", { className: "big-icon " + (q.icon || "icon-settings") }),
                _(
                    CardBlock, { className: "pb-0" },
                    _(
                        "div", {
                            className: "float-right",
                            style: {
                                fontSize: "30px",
                                background: "white",
                                padding: "0 13px",
                                borderRadius: "55px",
                                color: "darkorange"
                            }
                        },
                        q.xvalue
                    ),
                    _("h1", { className: "mb-0" }, q.dsc),
                    _("div", { style: { height: "25px" } })
                )
            );
        }
        name = name.name;
        var cmp = null;
        if (o.graph) {
            return _(
                Card, {
                    className: "card-portlet " + (o.props.color ? "bg-" + o.props.color : "")
                },
                _(
                    "h3", {
                        className: "form-header",
                        style: {
                            fontSize: "1.5rem",
                            padding: "10px 12px 0px",
                            marginBottom: ".5rem"
                        }
                    },
                    name,
                    _("i", { className: "portlet-refresh float-right icon-refresh", onClick:this.reloadItem })
                    
                ),
                _("div", {
                    style: { width: "100%", height: o.props.height || "20vw" },
                    id: "idG" + o.graph.graphId
                })
            );
        } else if (o.grid) {
            o.grid.crudFlags = false;
            return _(
                Card, {
                    className: "xportlet-grid-"+o.grid.gridId+" card-portlet " + (o.props.color ? "bg-" + o.props.color : "")
                },
                _(
                    "h3", {
                        className: "form-header",
                        style: {
                            fontSize: "1.5rem",
                            padding: "10px 7px 0px",
                            marginBottom: ".5rem"
                        }
                    },
                    name,
                    !o.grid._hideRefreshBtn && _("i", { style:{cursor:'pointer'}, title:'Refresh', className: "portlet-refresh float-right icon-refresh", onClick:this.reloadItem }),
                    o.grid._initHiddenFilters && _("i", { style:{cursor:'pointer'}, title:'Filters', className: "portlet-refresh float-right icon-magnifier", onClick:()=>o.grid.cmp.toggleFilters()}),
	                (o.grid.extraButtons || []).map((btn, index) =>
	                	_("i", { style:{cursor:'pointer', marginRight:5}, title:btn.text, className: "portlet-refresh float-right "+(btn.icon||'icon-heart'), onClick:btn.click })
	                )
                ),
                _(XGrid, Object.assign({}, o.grid, {registerLoad:(fx)=>{
            		if(fx)this.reloadFnc=fx;
            	}}))
            );
        } else if (o.gquery) {
        		return _(
                    Card, {
                        className: "card-portlet " + (o.props.color ? "bg-" + o.props.color : "")
                    },
                    _(
                        "h3", {
                            className: "form-header",
                            style: {
                                fontSize: "1.5rem",
                                padding: "10px 12px 0px",
                                marginBottom: ".5rem"
                            }
                        },
                        name,
                        _("i", { className: "portlet-refresh float-right icon-refresh", onClick:this.reloadItem })
                        
                    ),
                    _("div", {
                        style: { width: "100%", height: o.props.height || "20vw" },
                        id: "idGQ" + o.gquery.queryId
                    })
                );
        } else if (o.card){ 
        	o.card.crudFlags = false;
            return _(
                Card, {
                    className: "card-portlet " + (o.props.color ? "bg-" + o.props.color : "")
                },
                _(
                    "h3", {
                        className: "form-header",
                        style: {
                            fontSize: "1.5rem",
                            padding: "10px 12px 0px",
                            marginBottom: ".5rem"
                        }
                    },
                    name,
                    _("i", { className: "portlet-refresh float-right icon-refresh", onClick:this.reloadItem }),
                    this.props.filter && this.props.filter.items && 
                    	_("select", { onChange:(e)=>{
//                    		console.log(e.target.value);
                    		var params ={}
                    		params[this.props.filter.name] = e.target.value; 
                    		this.reloadFnc(!0, params);	
                    	}, name:this.props.filter.name
                    		, style: {fontSize: 17, color: '#888', marginRight: 10, marginTop: 1, float: 'right'}},
                    		this.props.filter.items.map(oo=>_('option',{value:oo.id},oo.dsc)))
                ),
                _(XCardList, Object.assign({}, o.card, {registerLoad:(fx)=>{
            		if(fx)this.reloadFnc=fx;
               	}}))
            );
        
        } else if (o.page){ 
            return _(
                    Card, {
                        className: "card-portlet " + (o.props.color ? "bg-" + o.props.color : "")
                    },o.page());
        
        } else if (o.gauge){ 
            return _(
                Card, {
                    className: "card-portlet " + (o.props.color ? "bg-" + o.props.color : "")
                },
	                _("div", {
	                    style: { width: "100%", height: o.props.height || "20vw" },
	                    id: "idGA" + o.gauge
	                })
                );
        
        }
        else if (o.component) return o.component;
        return false;
    }
}


class XDashboard extends React.PureComponent {
    constructor(props) {
        super(props);
        this.state = {counter:0};
        this.reloadFncs={};

        this.reloadAll = (xparams) => {
        	this.setState({counter:this.state.counter+1});
//        	console.log(this.reloadFncs)
        	for(var k in this.reloadFncs)this.reloadFncs[k](!0);
        }
    }

    
    render(){
    	var o = this.props;
        if (!o || !o.rows || !o.rows.length)
            return iwb.debug ? _("div", null, "No portlets defined"): false;
        return _('div',{},
        		o.reload &&_(Row, {style:{marginTop: -35,marginBottom: 10}},_(Col, {xs:12},_(Button,{style:{float:'right'},color: "secondary",
                    onClick: event => this.reloadAll()},getLocMsg('refresh_all')))),
       		o.rows.map((rowItem, rowIndex) => {
	            return _(Row, {
	                key: "xp-"+rowIndex,
	                children: rowItem.map((colItem, colIndex) =>
	                    _(Col, colItem.props||{}, _(XPortletItem, Object.assign({}, colItem, {registerLoad:(fx)=>{
	                		if(fx){
	                			var id = "xx-"+rowIndex;
	                			if(colItem.graph)id="pgraph-"+colItem.graph.graphId;
	                			else if(colItem.grid)id="pgrid-"+colItem.grid.gridId;
	                			else if(colItem.card)id="pcard-"+colItem.card.cardId;
	                			else if(colItem.query)id="pquery-"+colItem.query.queryId;
	                			else if(colItem.gquery)id="pgquery-"+colItem.gquery.queryId;
	                			else if(colItem.gauge)id="pgauge-"+colItem.gauge;
	                			this.reloadFncs[id]=fx;
	                		}
	                   	}}))) //iwb.createPortlet(colItem
	                )
	            });
	        }));
    }
    
}

iwb.ui.buildDashboard = function(o) {
    if (!o || !o.rows || !o.rows.length)
        return iwb.debug ? _("div", null, "No portlets defined"): false;
    return o.rows.map((rowItem, rowIndex) => {
        return _(Row, {
            key: rowIndex,
            children: rowItem.map((colItem, colIndex) =>
                _(Col, colItem.props||{}, _(XPortletItem, colItem)) //iwb.createPortlet(colItem
            )
        });
    });
};

iwb.ajax = {};
iwb.ajax.query = function(queryId, params, callback) {
    iwb.request({
        url: "ajaxQueryData?_qid=" + queryId,
        params: params || {},
        successCallback: callback || false
    });
};
iwb.ajax.postForm = function(formId, action, params, callback) {
    iwb.request({
        url: "ajaxPostForm?_fid=" + formId + "&a=" + action,
        params: params || {},
        successCallback: callback || false
    });
};
iwb.ajax.execFunc = function(funcId, params, callback) {
    iwb.request({
        url: "ajaxExecDbFunc?_did=" + funcId,
        params: params || {},
        successCallback: callback || false
    });
};
iwb.ajax.REST = function(serviceName, params, callback) {
    iwb.request({
        url: "ajaxCallWs?serviceName=" + serviceName,
        params: params || {},
        successCallback: callback || false
    });
};

function approvalHtml(row) {
    // console.log('approvalHtml', row);
    if (!row || !row.pkpkpk_arf_qw_) return "";
    switch (1 * row.pkpkpk_arf) {
        case 901:
        case -901:
            return iwb.label.startApprovalManually || "Start approval manually";
        case 998:
            return _(
                "a", {
                    href: "#",
                    className: "badge badge-pill badge-success",
                    onClick: iwb.approvalLogs(row.pkpkpk_arf_id)
                },
                iwb.label.approved || getLocMsg("approved")
            );
        case -999:
        case 999:
            return _(
                "a", {
                    href: "#",
                    className: "badge badge-pill badge-danger",
                    onClick: iwb.approvalLogs(row.pkpkpk_arf_id)
                },
                iwb.label.rejected || getLocMsg("rejected")
            );
        default:
            return _(
                "a", {
                    href: "#",
                    title:
                        (row.app_user_ids_qw_ ? ": " + row.app_user_ids_qw_ : "") +
                        " " +
                        (row.app_role_ids_qw_ ? "\n: " + row.app_role_ids_qw_ : ""),
                    onClick: iwb.approvalLogs(row.pkpkpk_arf_id)
                },
                1 * row.pkpkpk_arf ?
                _("i", {
                    className: "icon-shuffle",
                    style: 1 * row.pkpkpk_arf > 0 ? { color: "red" } : null
                }) :
                null,
                " " + row.pkpkpk_arf_qw_
            );
    }
}
iwb.fmtFileSize = a => {
    if (!a) return "-";
    a *= 1;
    var d = "B";
    if (a > 1024) {
        a = a / 1024;
        d = "KB";
    }
    if (a > 1024) {
        a = a / 1024;
        d = "MB";
    }
    if (a > 1024) {
        a = a / 1024;
        d = "GB";
    }
    if (d != "B") a = Math.round(a * 10) / 10;
    return a + " " + d;
};

function commentHtml(row, cell){
    return row[cell] && 1 * row[cell] ?
            _("i", { className: "icon-bubble" }) :
            null;
}


class CheckboxGroup extends React.Component {
    constructor(props) {
        super(props);
    }
    componentDidMount() {
    }
    render() {
    	var valMap={}
    	if(typeof this.props.value!='undefined'){
	    	if(!this.props.multi){
	    		valMap[this.props.value]=!0;
	    	} else {
	    		this.props.value.map((o)=>valMap[o]=!0);
	    	}
    	}
        return _("div", {
            style: {padding:5},
            
        }, this.props.options.map((o) => {
	        	return _('div',{}
	            	,_('input',{id:this.props.name+'_'+o[this.props.valueKey], type:this.props.multi?'checkbox':'radio', checked:valMap[o[this.props.valueKey]], onClick:(aq)=>{
	            		this.props.onChange({id:aq.target.value, checked:aq.target.checked});
	            	}, name:this.props.name, value:o[this.props.valueKey]})
		        	,' ', _('label',{for:this.props.name+'_'+o[this.props.valueKey], style:{fontWeight:400,color:'#333'}}, o[this.props.labelKey]));
        		}
        	)
        );
    }
}

iwb.hasPartInside=function(all,sub){
	if(typeof all=='undefined')return false;
	if((''+all).length==0)return false;
	if((','+all+',').indexOf(','+sub+',')==-1)return false;
	return true;
}
iwb.safeEquals= function(v1, v2){
	if(v1==='' || v1===false || (typeof v1=='undefined')){
		return (v2==='' || (typeof v2=='undefined'));
	} else if(v2==='' || (typeof v2=='undefined'))return false;
	return v1==v2;
}

iwb.formElementProperty = function(opr, elementValue, value){
	switch(1*opr){
	case -1://is Empty
		return elementValue==='' || elementValue===null || (typeof elementValue=='undefined');
	case -2://is not empty
		return !(elementValue==='' || elementValue===null || (typeof elementValue=='undefined'));
	case	8://in
		if(value==='' || (typeof value=='undefined'))return false;
		return iwb.hasPartInside(value, elementValue);
	case	9://not in
		if(value==='' || (typeof value=='undefined'))return true;
		return !iwb.hasPartInside(value, elementValue);
	case	0://equals
		return iwb.safeEquals(elementValue, value);
	case	1://not equals
		return !iwb.safeEquals(elementValue, value);
		
	}
	return false;
	
}
iwb.hideColumn= function(columns,name){
	columns.map(o => {
		if(o.name == name)o.hidden=true; 
	})
}

function extractSurveyJsResult(o){
	if(o && Array.isArray(o)){
		if(o.length && o[0].content){
			return o[0].content.substr(o[0].content.lastIndexOf('=')+1);
		} else 
			return o.join(','); 
			
	} else 
		return o;
}

iwb.postSurveyJs=(formId, action, params, surveyData, masterParams)=>{
//	console.log(params)
	var params2 = {_mask:!0}, fid = 0;
	if(masterParams)for(var kk in masterParams)params2[kk] = masterParams[kk];
	for(var k in params){
		var o = params[k];
		if(k.startsWith('_form_')){
			fid++;
			params2['_fid'+fid] = k.substr('_form_'.length);
			if(action==2 || !surveyData[k] || !surveyData[k].length){
				params2['_cnt'+fid] = o.length;
				for(var qi=0;qi<o.length;qi++){
					var cell = o[qi];
					params2['a'+fid+'.'+(qi+1)] = 2;
					for(var kk in cell){
						params2[kk+fid+'.'+(qi+1)] = extractSurveyJsResult(cell[kk]);
					}
					if(masterParams)for(var kk in masterParams)
						params2[kk.substr(1)+fid+'.'+(qi+1)] = extractSurveyJsResult(masterParams[kk]);
				}			
			} else {//update
				var s = surveyData[k], cnt = 0, pkFieldName='';
				var sm = {}
				s.map(scell => {
					for(var sk in scell)if(sk.startsWith('_id_')){
						sm[scell[sk]]=scell;
						pkFieldName = sk.substr('_id_'.length);
					}
				});
				
				for(var qi=0;qi<o.length;qi++){//for each fresh data
					var cell = o[qi];
					cnt++;
					params2['a'+fid+'.'+cnt] = 2;
					for(var kk in cell)if(kk.startsWith('_id_')){
						params2['a'+fid+'.'+cnt] = 1;
						params2['t'+pkFieldName+fid+'.'+cnt] = cell[kk];
						delete sm[cell[kk]];
					} else {
						params2[kk+fid+'.'+(qi+1)] = extractSurveyJsResult(cell[kk]);
					}
					if(masterParams)for(var kk in masterParams)
						params2[kk.substr(1)+fid+'.'+cnt] = extractSurveyJsResult(masterParams[kk]);
				}
				for(var sk in sm){
					cnt++;
					params2['a'+fid+'.'+cnt] = 3;
					params2['t'+pkFieldName+fid+'.'+cnt] = sk;
				}
				if(cnt)
					params2['_cnt'+fid] = cnt;
				else {
					delete params2['_fid'+fid];
					fid--;
				}
			}
		} else {
			params2[k] =  extractSurveyJsResult(o);
		}
	}
	if(action==1)for(var k in surveyData)if(k.startsWith('_form_') && surveyData[k] && surveyData[k].length && 
			(!params[k] || !params[k].length)){
		var o = surveyData[k];
		fid++;
		params2['_fid'+fid] = k.substr('_form_'.length);
		params2['_cnt'+fid] = o.length;
		for(var qi=0;qi<o.length;qi++){
			var cell = o[qi];
			params2['a'+fid+'.'+(qi+1)] = 3;
			for(var kk in cell)if(kk.startsWith('_id_')){
				params2['t'+kk.substr('_id_'.length)+fid+'.'+(qi+1)] = cell[kk];
			}
		}
	}
	iwb.ajax.postForm(formId, action, params2, ()=>{
		toastr.success(
            "",
            "Saved Successfully", {
                timeOut: 3000
            }
        );
		iwb.closeTab({}, !0);
	})
}

iwb.fileUploadSurveyJs=(tableId, tablePk, survey, options)=>{
	var formData = new FormData();
    options
        .files
        .forEach(function (file) {
            formData.append("file", file);
            formData.append("table_id", tableId);
            formData.append("table_pk", tablePk);
            formData.append("profilePictureFlag", 0);
        });
    var xhr = new XMLHttpRequest();
    xhr.responseType = "json";
    xhr.open("POST", "upload.form"); // https://surveyjs.io/api/MySurveys/uploadFiles
    xhr.onload = function () {
        var data = xhr.response;
        options.callback("success", options.files.map(file => {
            return {
                file: file,
                content: data.fileUrl
            };
        }));
    };
    xhr.send(formData);
}

function gcx(w, h, r) {
  var l = (screen.width - w) / 2;
  var t = (screen.height - h) / 2;
  r = r ? 1 : 0;
  return (
    "toolbar=0,scrollbars=0,location=0,status=1,menubar=0,resizable=" +
    r +
    ",width=" +
    w +
    ",height=" +
    h +
    ",left=" +
    l +
    ",top=" +
    t
  );
}

function openPopup(url, name, x, y, r) {
  var wh = window.open(url, name, gcx(x, y, r));
  if (!wh) toastr.error(getLocMsg("remove_popup_blocker"));
  else wh.focus();
  return false;
}

iwb.findAsyncValue = function(value, options){
	if(!value || !options || !options.length)return '';
	for(var qi=0;qi<options.length;qi++)if(options[qi].id==value)return options[qi];
	return '';
	
}