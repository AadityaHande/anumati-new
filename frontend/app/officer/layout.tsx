import { DepartmentShell } from "../../components/department-shell";

export default function OfficerLayout({ children }: { children: React.ReactNode }) {
  return <DepartmentShell>{children}</DepartmentShell>;
}
