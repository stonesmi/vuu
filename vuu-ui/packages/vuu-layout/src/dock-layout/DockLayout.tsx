import {
  asReactElements,
  partition,
  registerComponent,
} from "@vuu-ui/vuu-utils";
import cx from "clsx";
import { useComponentCssInjection } from "@salt-ds/styles";
import { useWindow } from "@salt-ds/window";
import { HTMLAttributes, ReactElement, useMemo } from "react";
import Drawer from "./Drawer";

import dockLayoutCss from "./DockLayout.css";

const isDrawer = (component: ReactElement) => component.type === Drawer;
const isVertical = (element: ReactElement) =>
  // eslint-disable-next-line @typescript-eslint/no-explicit-any
  (element.props as any).position &&
  // eslint-disable-next-line @typescript-eslint/no-explicit-any
  (element.props as any).position.match(/top|bottom/);

export type DockLayoutProps = HTMLAttributes<HTMLDivElement>;

const classBase = "vuuDockLayout";

const DockLayout = (props: DockLayoutProps) => {
  const targetWindow = useWindow();
  useComponentCssInjection({
    testId: "vuu-dock-layput",
    css: dockLayoutCss,
    window: targetWindow,
  });

  const { children, className: classNameProp, id, style } = props;
  const childElements = useMemo(() => asReactElements(children), [children]);

  const [drawers, content] = partition(childElements, isDrawer);
  const [verticalDrawers, horizontalDrawers] = partition(drawers, isVertical);
  const orientation =
    verticalDrawers.length === 0
      ? "horizontal"
      : horizontalDrawers.length === 0
        ? "vertical"
        : "both";

  const className = cx(classBase, classNameProp, `${classBase}-${orientation}`);

  return (
    <div className={className} id={id} style={style}>
      {drawers}
      <div className={`${classBase}-content`}>{content}</div>
    </div>
  );
};
DockLayout.displayName = "DockLayout";

export default DockLayout;

registerComponent("DockLayout", DockLayout, "container");
